package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.protocol.GameResultType

@Composable
fun GameOverScreen(
    gameState: org.example.project.protocol.ServerMessage.GameState,
    gameResult: org.example.project.protocol.ServerMessage.GameResult,
    onNewGame: () -> Unit,
    onShowRecords: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .padding(16.dp)
    ) {
        // Barra superior
        TopBar(
            onShowRecords = onShowRecords,
            onDisconnect = onDisconnect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mesa de juego con resultado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Resultado
            ResultCard(gameResult)

            Spacer(modifier = Modifier.height(32.dp))

            // Mano del Dealer (todas las cartas reveladas)
            DealerHand(
                cards = gameResult.dealerFinalHand,
                score = gameResult.dealerFinalScore,
                gamePhase = org.example.project.protocol.GamePhase.GAME_OVER
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mano del Jugador
            PlayerHand(
                cards = gameState.playerHand,
                score = gameResult.playerFinalScore
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controles
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNewGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("🎴 Nueva Partida", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButton(
                    onClick = onShowRecords,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🏆 Ver Records", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun ResultCard(gameResult: org.example.project.protocol.ServerMessage.GameResult) {
    val (emoji, resultText, backgroundColor) = when (gameResult.result) {
        GameResultType.BLACKJACK -> Triple(
            "🎰",
            "¡BLACKJACK!",
            Color(0xFFFFD700) // Dorado
        )
        GameResultType.WIN -> Triple(
            "🎉",
            "¡GANASTE!",
            Color(0xFF4CAF50) // Verde
        )
        GameResultType.LOSE -> Triple(
            "💔",
            "PERDISTE",
            Color(0xFFF44336) // Rojo
        )
        GameResultType.PUSH -> Triple(
            "🤝",
            "EMPATE",
            Color(0xFFFF9800) // Naranja
        )
    }

    Card(
        modifier = Modifier
            .widthIn(max = 500.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resultText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = gameResult.message,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tu mano",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "${gameResult.playerFinalScore}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Dealer",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "${gameResult.dealerFinalScore}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
