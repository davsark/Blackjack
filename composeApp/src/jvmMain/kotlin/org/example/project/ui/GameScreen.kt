package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.protocol.Card
import org.example.project.protocol.GamePhase

@Composable
fun GameScreen(
    gameState: org.example.project.protocol.ServerMessage.GameState,
    onRequestCard: () -> Unit,
    onStand: () -> Unit,
    onNewGame: () -> Unit,
    onShowRecords: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)) // Verde oscuro de mesa de casino
            .padding(16.dp)
    ) {
        // Barra superior
        TopBar(
            onShowRecords = onShowRecords,
            onDisconnect = onDisconnect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mesa de juego
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mano del Dealer
            DealerHand(
                cards = gameState.dealerHand,
                score = gameState.dealerScore,
                gamePhase = gameState.gameState
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mano del Jugador
            PlayerHand(
                cards = gameState.playerHand,
                score = gameState.playerScore
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controles
        GameControls(
            canRequestCard = gameState.canRequestCard,
            canStand = gameState.canStand,
            gamePhase = gameState.gameState,
            onRequestCard = onRequestCard,
            onStand = onStand,
            onNewGame = onNewGame
        )
    }
}

@Composable
fun TopBar(
    onShowRecords: () -> Unit,
    onDisconnect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "🎰 BLACKJACK",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowRecords,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("🏆 Records")
            }

            OutlinedButton(
                onClick = onDisconnect,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Desconectar")
            }
        }
    }
}

@Composable
fun DealerHand(
    cards: List<Card>,
    score: Int,
    gamePhase: GamePhase
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DEALER",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (gamePhase == GamePhase.GAME_OVER) {
                "Puntuación: $score"
            } else {
                "Carta visible: $score"
            },
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cartas del dealer
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            cards.forEach { card ->
                CardView(card)
            }
        }
    }
}

@Composable
fun PlayerHand(
    cards: List<Card>,
    score: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TU MANO",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Puntuación: $score",
            style = MaterialTheme.typography.titleMedium,
            color = when {
                score == 21 -> Color(0xFFFFD700) // Dorado
                score > 21 -> Color(0xFFFF5252) // Rojo
                else -> Color.White
            },
            fontWeight = if (score == 21) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cartas del jugador
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            cards.forEach { card ->
                CardView(card)
            }
        }
    }
}

@Composable
fun CardView(card: Card) {
    val backgroundColor = if (card.hidden) {
        Color(0xFF1976D2) // Azul para cartas ocultas
    } else {
        Color.White
    }

    val textColor = if (card.hidden) {
        Color.White
    } else {
        when (card.suit.displayName) {
            "Corazones", "Diamantes" -> Color.Red
            else -> Color.Black
        }
    }

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (card.hidden) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.displayLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.rank.symbol,
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.suit.symbol,
                        style = MaterialTheme.typography.headlineMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun GameControls(
    canRequestCard: Boolean,
    canStand: Boolean,
    gamePhase: GamePhase,
    onRequestCard: () -> Unit,
    onStand: () -> Unit,
    onNewGame: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gamePhase) {
                GamePhase.PLAYER_TURN -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onRequestCard,
                            enabled = canRequestCard,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("🎴 PEDIR", style = MaterialTheme.typography.titleMedium)
                        }

                        Button(
                            onClick = onStand,
                            enabled = canStand,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            )
                        ) {
                            Text("✋ PLANTARSE", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                GamePhase.DEALER_TURN -> {
                    Text(
                        text = "⏳ Turno del Dealer...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                else -> {
                    Button(
                        onClick = onNewGame,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nueva Partida", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
