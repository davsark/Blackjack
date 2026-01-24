package org.example.project.model

import org.example.project.protocol.Card
import org.example.project.protocol.Rank
import org.example.project.protocol.Suit

/**
 * Representa una baraja de cartas estándar (52 cartas)
 */
class Deck {
    private val cards = mutableListOf<Card>()

    init {
        reset()
    }

    /**
     * Resetea la baraja con todas las 52 cartas
     */
    fun reset() {
        cards.clear()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                cards.add(Card(rank, suit, hidden = false))
            }
        }
    }

    /**
     * Baraja las cartas aleatoriamente
     */
    fun shuffle() {
        cards.shuffle()
    }

    /**
     * Reparte una carta de la parte superior
     * @param hidden Si la carta debe estar oculta (para el dealer)
     * @return La carta repartida
     * @throws IllegalStateException si no quedan cartas
     */
    fun dealCard(hidden: Boolean = false): Card {
        if (cards.isEmpty()) {
            throw IllegalStateException("No quedan cartas en la baraja")
        }
        val card = cards.removeFirst()
        return card.copy(hidden = hidden)
    }

    /**
     * Número de cartas restantes en la baraja
     */
    fun remainingCards(): Int = cards.size

    /**
     * Verifica si la baraja necesita ser reemplazada
     * (normalmente cuando quedan menos de 15 cartas)
     */
    fun needsReset(): Boolean = cards.size < 15

    override fun toString(): String {
        return "Deck(cartas restantes: ${cards.size})"
    }
}
