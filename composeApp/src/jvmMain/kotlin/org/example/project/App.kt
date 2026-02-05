package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.protocol.GameMode
import org.example.project.ui.*
import org.example.project.viewmodel.GameUiState
import org.example.project.viewmodel.GameViewModel
import kotlin.system.exitProcess

@Composable
fun App() {
    MaterialTheme {
        val viewModel: GameViewModel = viewModel { GameViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val currentGameState by viewModel.currentGameState.collectAsState()
        val gameResult by viewModel.gameResult.collectAsState()
        val records by viewModel.records.collectAsState()
        val betInfo by viewModel.betInfo.collectAsState()
        val numberOfDecks by viewModel.numberOfDecks.collectAsState()

        when (uiState) {
            is GameUiState.MainMenu -> {
                MainMenuScreen(
                    onPlayPVE = {
                        viewModel.startPVE()
                    },
                    onPlayPVP = {
                        viewModel.startPVP()
                    },
                    onShowRecords = {
                        // Conectar primero para obtener records
                        viewModel.connect("localhost", 9999)
                    },
                    onShowConfig = {
                        viewModel.showConfig()
                    },
                    onExit = {
                        exitProcess(0)
                    }
                )
            }

            is GameUiState.ShowingConfig -> {
                ConfigScreen(
                    currentDecks = numberOfDecks,
                    onDecksChange = { viewModel.setNumberOfDecks(it) },
                    onBack = { viewModel.backToMenu() }
                )
            }

            is GameUiState.Connecting -> {
                ConnectionScreen(
                    onConnect = { host, port ->
                        viewModel.connect(host, port)
                    }
                )
            }

            is GameUiState.Connected -> {
                JoinGameScreen(
                    onJoinGame = { playerName, gameMode ->
                        viewModel.joinGame(playerName, gameMode)
                    }
                )
            }

            is GameUiState.WaitingForGame -> {
                LoadingScreen("Iniciando partida...")
            }

            is GameUiState.Betting -> {
                betInfo?.let { info ->
                    BettingScreen(
                        playerChips = info.playerChips,
                        minBet = info.minBet,
                        maxBet = info.maxBet,
                        onPlaceBet = { amount -> viewModel.placeBet(amount) },
                        onBack = { viewModel.disconnect() }
                    )
                } ?: LoadingScreen("Cargando...")
            }

            is GameUiState.InGame -> {
                currentGameState?.let { state ->
                    GameScreen(
                        gameState = state,
                        onRequestCard = { viewModel.requestCard() },
                        onStand = { viewModel.stand() },
                        onDouble = { viewModel.double() },
                        onSplit = { viewModel.split() },
                        onSurrender = { viewModel.surrender() },
                        onNewGame = { viewModel.newGame() },
                        onShowRecords = { viewModel.requestRecords() },
                        onDisconnect = { viewModel.disconnect() }
                    )
                }
            }

            is GameUiState.GameOver -> {
                gameResult?.let { result ->
                    currentGameState?.let { state ->
                        GameOverScreen(
                            gameState = state,
                            gameResult = result,
                            onNewGame = { viewModel.newGame() },
                            onShowRecords = { viewModel.requestRecords() },
                            onDisconnect = { viewModel.disconnect() }
                        )
                    }
                }
            }

            is GameUiState.ShowingRecords -> {
                RecordsScreen(
                    records = records,
                    onBack = { viewModel.backToGame() }
                )
            }

            is GameUiState.Error -> {
                ErrorScreen(
                    message = (uiState as GameUiState.Error).message,
                    onDismiss = { viewModel.clearError() },
                    onDisconnect = { viewModel.disconnect() }
                )
            }
        }
    }
}
