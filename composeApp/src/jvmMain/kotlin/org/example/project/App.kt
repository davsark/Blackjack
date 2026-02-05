package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.ui.*
import org.example.project.viewmodel.GameUiState
import org.example.project.viewmodel.GameViewModel

@Composable
fun App() {
    MaterialTheme {
        val viewModel: GameViewModel = viewModel { GameViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val currentGameState by viewModel.currentGameState.collectAsState()
        val gameResult by viewModel.gameResult.collectAsState()
        val records by viewModel.records.collectAsState()

        when (uiState) {
            is GameUiState.Disconnected -> {
                ConnectionScreen(
                    onConnect = { host, port ->
                        viewModel.connect(host, port)
                    }
                )
            }

            is GameUiState.Connecting -> {
                LoadingScreen("Conectando al servidor...")
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

            is GameUiState.InGame -> {
                currentGameState?.let { state ->
                    GameScreen(
                        gameState = state,
                        onRequestCard = { viewModel.requestCard() },
                        onStand = { viewModel.stand() },
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
