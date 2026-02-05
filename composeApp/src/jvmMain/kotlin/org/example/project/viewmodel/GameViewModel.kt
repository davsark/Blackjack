package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.project.network.GameClient
import org.example.project.protocol.*

/**
 * ViewModel que gestiona el estado del juego
 */
class GameViewModel : ViewModel() {
    private val gameClient = GameClient()

    // Estado de la UI
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Disconnected)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Estado del juego actual
    private val _currentGameState = MutableStateFlow<ServerMessage.GameState?>(null)
    val currentGameState: StateFlow<ServerMessage.GameState?> = _currentGameState.asStateFlow()

    // Resultado del juego
    private val _gameResult = MutableStateFlow<ServerMessage.GameResult?>(null)
    val gameResult: StateFlow<ServerMessage.GameResult?> = _gameResult.asStateFlow()

    // Records
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records.asStateFlow()

    // Información del jugador
    private val _playerId = MutableStateFlow<String?>(null)
    val playerId: StateFlow<String?> = _playerId.asStateFlow()

    init {
        // Observar mensajes del servidor
        viewModelScope.launch {
            gameClient.serverMessages.collect { message ->
                message?.let { handleServerMessage(it) }
            }
        }

        // Observar errores de conexión
        viewModelScope.launch {
            gameClient.connectionError.collect { error ->
                error?.let {
                    _uiState.value = GameUiState.Error(it)
                }
            }
        }

        // Observar estado de conexión
        viewModelScope.launch {
            gameClient.isConnected.collect { connected ->
                if (!connected && _uiState.value !is GameUiState.Disconnected) {
                    _uiState.value = GameUiState.Disconnected
                }
            }
        }
    }

    /**
     * Conecta al servidor
     */
    fun connect(host: String, port: Int) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Connecting
            val success = gameClient.connect(host, port)
            if (success) {
                _uiState.value = GameUiState.Connected
            } else {
                _uiState.value = GameUiState.Disconnected
            }
        }
    }

    /**
     * Se une al juego
     */
    fun joinGame(playerName: String, gameMode: GameMode) {
        viewModelScope.launch {
            val message = ClientMessage.JoinGame(playerName, gameMode)
            gameClient.sendMessage(message)
            _uiState.value = GameUiState.WaitingForGame
        }
    }

    /**
     * Pide una carta
     */
    fun requestCard() {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.RequestCard)
        }
    }

    /**
     * Se planta
     */
    fun stand() {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.Stand)
        }
    }

    /**
     * Solicita una nueva partida
     */
    fun newGame() {
        viewModelScope.launch {
            _gameResult.value = null
            gameClient.sendMessage(ClientMessage.NewGame)
        }
    }

    /**
     * Solicita los records
     */
    fun requestRecords() {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.RequestRecords)
        }
    }

    /**
     * Maneja los mensajes del servidor
     */
    private fun handleServerMessage(message: ServerMessage) {
        when (message) {
            is ServerMessage.JoinConfirmation -> {
                _playerId.value = message.playerId
                _uiState.value = GameUiState.InGame
                println("✅ ${message.message}")
            }

            is ServerMessage.GameState -> {
                _currentGameState.value = message
                _uiState.value = GameUiState.InGame
            }

            is ServerMessage.GameResult -> {
                _gameResult.value = message
                _uiState.value = GameUiState.GameOver
            }

            is ServerMessage.RecordsList -> {
                _records.value = message.records
                _uiState.value = GameUiState.ShowingRecords
            }

            is ServerMessage.Error -> {
                _uiState.value = GameUiState.Error(message.errorMessage)
                println("❌ Error del servidor: ${message.errorMessage}")
            }

            is ServerMessage.Pong -> {
                // Ignorar por ahora
            }
        }
        gameClient.clearLastMessage()
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        if (_uiState.value is GameUiState.Error) {
            _uiState.value = if (gameClient.isConnected.value) {
                GameUiState.Connected
            } else {
                GameUiState.Disconnected
            }
        }
        gameClient.clearError()
    }

    /**
     * Vuelve al juego desde la pantalla de records
     */
    fun backToGame() {
        _uiState.value = if (_currentGameState.value != null) {
            GameUiState.InGame
        } else {
            GameUiState.Connected
        }
    }

    /**
     * Desconecta del servidor
     */
    fun disconnect() {
        gameClient.disconnect()
        _uiState.value = GameUiState.Disconnected
        _currentGameState.value = null
        _gameResult.value = null
        _playerId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        gameClient.disconnect()
    }
}

/**
 * Estados de la UI
 */
sealed class GameUiState {
    data object Disconnected : GameUiState()
    data object Connecting : GameUiState()
    data object Connected : GameUiState()
    data object WaitingForGame : GameUiState()
    data object InGame : GameUiState()
    data object GameOver : GameUiState()
    data object ShowingRecords : GameUiState()
    data class Error(val message: String) : GameUiState()
}
