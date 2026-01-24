package org.example.project.protocol

import kotlinx.serialization.Serializable

/**
 * Mensajes del Cliente → Servidor
 */
@Serializable
sealed class ClientMessage {
    /**
     * El jugador se une al servidor
     * @param playerName Nombre del jugador
     * @param gameMode Modo de juego: "PVE" o "PVP"
     */
    @Serializable
    data class JoinGame(
        val playerName: String,
        val gameMode: GameMode
    ) : ClientMessage()

    /**
     * El jugador pide una carta (HIT)
     */
    @Serializable
    data object RequestCard : ClientMessage()

    /**
     * El jugador se planta (STAND)
     */
    @Serializable
    data object Stand : ClientMessage()

    /**
     * El jugador solicita empezar una nueva partida
     */
    @Serializable
    data object NewGame : ClientMessage()

    /**
     * El jugador solicita ver los records
     */
    @Serializable
    data object RequestRecords : ClientMessage()

    /**
     * Ping para mantener la conexión viva
     */
    @Serializable
    data object Ping : ClientMessage()
}

/**
 * Mensajes del Servidor → Cliente
 */
@Serializable
sealed class ServerMessage {
    /**
     * Confirmación de unión al juego
     * @param playerId ID único asignado al jugador
     * @param message Mensaje de bienvenida
     */
    @Serializable
    data class JoinConfirmation(
        val playerId: String,
        val message: String
    ) : ServerMessage()

    /**
     * Estado completo del juego
     * @param playerHand Cartas del jugador
     * @param dealerHand Cartas del dealer (algunas ocultas)
     * @param playerScore Puntuación del jugador
     * @param dealerScore Puntuación del dealer (puede ser parcial)
     * @param gameState Estado actual del juego
     * @param canRequestCard Si el jugador puede pedir carta
     * @param canStand Si el jugador puede plantarse
     */
    @Serializable
    data class GameState(
        val playerHand: List<Card>,
        val dealerHand: List<Card>,
        val playerScore: Int,
        val dealerScore: Int,
        val gameState: GamePhase,
        val canRequestCard: Boolean,
        val canStand: Boolean,
        val otherPlayers: List<PlayerInfo> = emptyList() // Para PVP
    ) : ServerMessage()

    /**
     * Resultado final de la partida
     * @param result Resultado: WIN, LOSE, PUSH
     * @param playerFinalScore Puntuación final del jugador
     * @param dealerFinalScore Puntuación final del dealer
     * @param message Mensaje descriptivo del resultado
     */
    @Serializable
    data class GameResult(
        val result: GameResultType,
        val playerFinalScore: Int,
        val dealerFinalScore: Int,
        val message: String,
        val dealerFinalHand: List<Card> // Revelar cartas ocultas del dealer
    ) : ServerMessage()

    /**
     * Lista de records
     * @param records Lista de los mejores jugadores
     */
    @Serializable
    data class RecordsList(
        val records: List<Record>
    ) : ServerMessage()

    /**
     * Error del servidor
     * @param errorMessage Descripción del error
     */
    @Serializable
    data class Error(
        val errorMessage: String
    ) : ServerMessage()

    /**
     * Respuesta al ping
     */
    @Serializable
    data object Pong : ServerMessage()
}

/**
 * Modos de juego
 */
@Serializable
enum class GameMode {
    PVE, // Jugador vs Dealer (IA)
    PVP  // Multijugador
}

/**
 * Fases del juego
 */
@Serializable
enum class GamePhase {
    WAITING,        // Esperando que empiece el juego
    PLAYER_TURN,    // Turno del jugador
    DEALER_TURN,    // Turno del dealer
    GAME_OVER       // Juego terminado
}

/**
 * Tipos de resultado
 */
@Serializable
enum class GameResultType {
    WIN,        // Jugador gana
    LOSE,       // Jugador pierde
    PUSH,       // Empate
    BLACKJACK   // Jugador tiene Blackjack natural
}

/**
 * Representación de una carta
 */
@Serializable
data class Card(
    val rank: Rank,
    val suit: Suit,
    val hidden: Boolean = false // Para cartas ocultas del dealer
) {
    override fun toString(): String {
        return if (hidden) "[OCULTA]" else "${rank.symbol}${suit.symbol}"
    }
}

/**
 * Palos de la baraja
 */
@Serializable
enum class Suit(val symbol: String, val displayName: String) {
    HEARTS("♥", "Corazones"),
    DIAMONDS("♦", "Diamantes"),
    CLUBS("♣", "Tréboles"),
    SPADES("♠", "Picas")
}

/**
 * Rangos de las cartas
 */
@Serializable
enum class Rank(val symbol: String, val values: List<Int>) {
    ACE("A", listOf(1, 11)),
    TWO("2", listOf(2)),
    THREE("3", listOf(3)),
    FOUR("4", listOf(4)),
    FIVE("5", listOf(5)),
    SIX("6", listOf(6)),
    SEVEN("7", listOf(7)),
    EIGHT("8", listOf(8)),
    NINE("9", listOf(9)),
    TEN("10", listOf(10)),
    JACK("J", listOf(10)),
    QUEEN("Q", listOf(10)),
    KING("K", listOf(10));

    /**
     * Valor principal de la carta (para cartas sin As es único)
     */
    val value: Int get() = values.first()
}

/**
 * Record de un jugador
 */
@Serializable
data class Record(
    val playerName: String,
    val wins: Int,
    val losses: Int,
    val blackjacks: Int,
    val timestamp: Long
) {
    val winRate: Double
        get() = if (wins + losses > 0) wins.toDouble() / (wins + losses) else 0.0
}

/**
 * Información de otros jugadores (para modo PVP)
 */
@Serializable
data class PlayerInfo(
    val playerName: String,
    val handSize: Int,
    val score: Int,
    val hasStood: Boolean
)
