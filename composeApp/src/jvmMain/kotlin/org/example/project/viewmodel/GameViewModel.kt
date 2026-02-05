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
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.MainMenu)
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

    // Información de apuestas
    private val _betInfo = MutableStateFlow<BetInfo?>(null)
    val betInfo: StateFlow<BetInfo?> = _betInfo.asStateFlow()

    // Configuración
    private val _numberOfDecks = MutableStateFlow(1)
    val numberOfDecks: StateFlow<Int> = _numberOfDecks.asStateFlow()

    // Modo de juego seleccionado
    private var selectedGameMode: GameMode = GameMode.PVE
    private var playerName: String = ""

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
                if (!connected && _uiState.value !is GameUiState.MainMenu && _uiState.value !is GameUiState.Error) {
                    _uiState.value = GameUiState.MainMenu
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
                _uiState.value = GameUiState.MainMenu
            }
        }
    }

    /**
     * Inicia el flujo de PVE
     */
    fun startPVE() {
        selectedGameMode = GameMode.PVE
        _uiState.value = GameUiState.Connecting
    }

    /**
     * Inicia el flujo de PVP
     */
    fun startPVP() {
        selectedGameMode = GameMode.PVP
        _uiState.value = GameUiState.Connecting
    }

    /**
     * Se une al juego
     */
    fun joinGame(playerName: String, gameMode: GameMode) {
        this.playerName = playerName
        this.selectedGameMode = gameMode
        viewModelScope.launch {
            val message = ClientMessage.JoinGame(playerName, gameMode)
            gameClient.sendMessage(message)
            _uiState.value = GameUiState.WaitingForGame
        }
    }

    /**
     * Realiza una apuesta
     */
    fun placeBet(amount: Int) {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.PlaceBet(amount))
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
     * Dobla la apuesta
     */
    fun double() {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.Double)
        }
    }

    /**
     * Divide la mano
     */
    fun split() {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.Split)
        }
    }

    /**
     * Se rinde
     */
    fun surrender() {
        viewModelScope.launch {
            gameClient.sendMessage(ClientMessage.Surrender)
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
     * Muestra la configuración
     */
    fun showConfig() {
        _uiState.value = GameUiState.ShowingConfig
    }

    /**
     * Actualiza el número de mazos
     */
    fun setNumberOfDecks(decks: Int) {
        _numberOfDecks.value = decks
    }

    /**
     * Maneja los mensajes del servidor
     */
    private fun handleServerMessage(message: ServerMessage) {
        when (message) {
            is ServerMessage.JoinConfirmation -> {
                _playerId.value = message.playerId
                _betInfo.value = _betInfo.value?.copy(currentChips = message.initialChips)
                    ?: BetInfo(message.initialChips, 10, 500, message.initialChips)
                println("✅ ${message.message}")
            }

            is ServerMessage.TableState -> {
                _betInfo.value = BetInfo(
                    currentChips = message.currentPlayerChips,
                    minBet = message.minBet,
                    maxBet = message.maxBet,
                    playerChips = message.currentPlayerChips
                )
            }

            is ServerMessage.RequestBet -> {
                _betInfo.value = BetInfo(
                    currentChips = message.currentChips,
                    minBet = message.minBet,
                    maxBet = message.maxBet,
                    playerChips = message.currentChips
                )
                _uiState.value = GameUiState.Betting
            }

            is ServerMessage.GameState -> {
                _currentGameState.value = message
                _betInfo.value = _betInfo.value?.copy(
                    currentChips = message.playerChips,
                    playerChips = message.playerChips
                )
                _uiState.value = GameUiState.InGame
            }

            is ServerMessage.GameResult -> {
                _gameResult.value = message
                _betInfo.value = _betInfo.value?.copy(
                    currentChips = message.newChipsTotal,
                    playerChips = message.newChipsTotal
                )
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
                // Ignorar
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
                if (_currentGameState.value != null) {
                    GameUiState.InGame
                } else {
                    GameUiState.Connected
                }
            } else {
                GameUiState.MainMenu
            }
        }
        gameClient.clearError()
    }

    /**
     * Vuelve al juego desde la pantalla de records
     */
    fun backToGame() {
        _uiState.value = when {
            _currentGameState.value != null -> GameUiState.InGame
            _betInfo.value != null && gameClient.isConnected.value -> GameUiState.Betting
            gameClient.isConnected.value -> GameUiState.Connected
            else -> GameUiState.MainMenu
        }
    }

    /**
     * Vuelve al menú principal
     */
    fun backToMenu() {
        _uiState.value = GameUiState.MainMenu
    }

    /**
     * Desconecta del servidor
     */
    fun disconnect() {
        gameClient.disconnect()
        _uiState.value = GameUiState.MainMenu
        _currentGameState.value = null
        _gameResult.value = null
        _playerId.value = null
        _betInfo.value = null
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
    data object MainMenu : GameUiState()
    data object ShowingConfig : GameUiState()
    data object Connecting : GameUiState()
    data object Connected : GameUiState()
    data object WaitingForGame : GameUiState()
    data object Betting : GameUiState()
    data object InGame : GameUiState()
    data object GameOver : GameUiState()
    data object ShowingRecords : GameUiState()
    data class Error(val message: String) : GameUiState()
}

/**
 * Información de apuestas
 */
data class BetInfo(
    val currentChips: Int,
    val minBet: Int,
    val maxBet: Int,
    val playerChips: Int
)
