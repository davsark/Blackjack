package org.example.project.server

import org.example.project.game.BlackjackGame
import org.example.project.model.Deck
import org.example.project.model.Hand
import org.example.project.protocol.*
import kotlinx.coroutines.delay

/**
 * Inteligencia Artificial del Dealer para modo PVE
 * Implementa las reglas estándar del Blackjack
 */
class DealerAI(private val deck: Deck) {
    private val dealerHand = Hand()
    private val playerHands = mutableMapOf<String, Hand>()

    /**
     * Inicia una nueva partida
     */
    fun startNewGame(playerId: String): ServerMessage.GameState {
        // Limpiar manos
        dealerHand.clear()
        playerHands.clear()

        // Crear mano para el jugador
        val playerHand = Hand()
        playerHands[playerId] = playerHand

        // Repartir cartas iniciales (2 para jugador, 2 para dealer)
        playerHand.addCard(deck.dealCard(hidden = false))
        dealerHand.addCard(deck.dealCard(hidden = false))
        playerHand.addCard(deck.dealCard(hidden = false))
        dealerHand.addCard(deck.dealCard(hidden = true)) // Segunda carta del dealer oculta

        println("🎴 Nueva partida PVE iniciada")
        println("   Jugador: ${playerHand.getCards()}")
        println("   Dealer: ${dealerHand.getCards()}")

        return buildGameState(playerId, GamePhase.PLAYER_TURN)
    }

    /**
     * Procesa la petición de carta del jugador
     */
    fun playerHit(playerId: String): ServerMessage.GameState {
        val playerHand = playerHands[playerId]
            ?: throw IllegalStateException("Jugador no encontrado")

        // Repartir carta al jugador
        val newCard = deck.dealCard(hidden = false)
        playerHand.addCard(newCard)

        println("🃏 Jugador pide carta: $newCard (Total: ${playerHand.calculateValue()})")

        // Verificar si el jugador se pasó
        return if (playerHand.isBusted()) {
            println("💥 ¡Jugador se pasó! (${playerHand.calculateValue()})")
            dealerHand.revealAll()
            buildGameState(playerId, GamePhase.GAME_OVER)
        } else if (playerHand.calculateValue() == 21) {
            // Si llega a 21, automáticamente pasa al turno del dealer
            println("🎯 Jugador alcanza 21, turno del dealer")
            playDealerTurn(playerId)
        } else {
            buildGameState(playerId, GamePhase.PLAYER_TURN)
        }
    }

    /**
     * Procesa cuando el jugador se planta
     */
    fun playerStand(playerId: String): ServerMessage.GameState {
        println("✋ Jugador se planta con ${playerHands[playerId]?.calculateValue()}")
        return playDealerTurn(playerId)
    }

    /**
     * El dealer juega su turno
     */
    private fun playDealerTurn(playerId: String): ServerMessage.GameState {
        // Revelar la carta oculta del dealer
        dealerHand.revealAll()
        println("👁️ Dealer revela su mano: ${dealerHand.getCards()} (${dealerHand.calculateValue()})")

        // El dealer pide cartas según las reglas
        while (BlackjackGame.shouldDealerHit(dealerHand)) {
            val newCard = deck.dealCard(hidden = false)
            dealerHand.addCard(newCard)
            println("🎴 Dealer pide carta: $newCard (Total: ${dealerHand.calculateValue()})")
        }

        if (dealerHand.isBusted()) {
            println("💥 ¡Dealer se pasó! (${dealerHand.calculateValue()})")
        } else {
            println("✋ Dealer se planta con ${dealerHand.calculateValue()}")
        }

        return buildGameState(playerId, GamePhase.GAME_OVER)
    }

    /**
     * Obtiene el resultado final del juego
     */
    fun getGameResult(playerId: String): ServerMessage.GameResult {
        val playerHand = playerHands[playerId]
            ?: throw IllegalStateException("Jugador no encontrado")

        val result = BlackjackGame.determineWinner(playerHand, dealerHand)
        val playerScore = playerHand.calculateValue()
        val dealerScore = dealerHand.calculateValue()
        val message = BlackjackGame.getResultMessage(result, playerScore, dealerScore)

        println("🏆 Resultado: $result - $message")

        return ServerMessage.GameResult(
            result = result,
            playerFinalScore = playerScore,
            dealerFinalScore = dealerScore,
            message = message,
            dealerFinalHand = dealerHand.getCards()
        )
    }

    /**
     * Construye el estado actual del juego
     */
    private fun buildGameState(playerId: String, phase: GamePhase): ServerMessage.GameState {
        val playerHand = playerHands[playerId]
            ?: throw IllegalStateException("Jugador no encontrado")

        val playerScore = playerHand.calculateValue()
        val dealerScore = if (phase == GamePhase.GAME_OVER) {
            dealerHand.calculateValue()
        } else {
            // Solo mostrar la carta visible del dealer
            dealerHand.getCards().firstOrNull { !it.hidden }?.rank?.value ?: 0
        }

        return ServerMessage.GameState(
            playerHand = playerHand.getCards(),
            dealerHand = dealerHand.getCards(),
            playerScore = playerScore,
            dealerScore = dealerScore,
            gameState = phase,
            canRequestCard = phase == GamePhase.PLAYER_TURN && BlackjackGame.canPlayerHit(playerHand),
            canStand = phase == GamePhase.PLAYER_TURN && BlackjackGame.canPlayerStand(playerHand)
        )
    }

    /**
     * Reinicia la baraja si es necesario
     */
    fun checkAndResetDeck() {
        if (deck.needsReset()) {
            println("🔄 Baraja baja en cartas, reiniciando...")
            deck.reset()
            deck.shuffle()
        }
    }
}
