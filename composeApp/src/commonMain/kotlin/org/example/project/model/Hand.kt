package org.example.project.model

import org.example.project.protocol.Card
import org.example.project.protocol.Rank

/**
 * Representa la mano de un jugador en Blackjack
 */
class Hand {
    private val cards = mutableListOf<Card>()

    /**
     * Añade una carta a la mano
     */
    fun addCard(card: Card) {
        cards.add(card)
    }

    /**
     * Obtiene todas las cartas de la mano
     */
    fun getCards(): List<Card> = cards.toList()

    /**
     * Limpia todas las cartas de la mano
     */
    fun clear() {
        cards.clear()
    }

    /**
     * Calcula el mejor valor posible de la mano
     *
     * En Blackjack, el As puede valer 1 u 11.
     * Esta función calcula el valor óptimo sin pasarse de 21.
     *
     * @return El mejor valor posible de la mano
     */
    fun calculateValue(): Int {
        var total = 0
        var aceCount = 0

        // Primera pasada: sumar valores sin considerar Ases como 11
        for (card in cards) {
            if (!card.hidden) {
                if (card.rank == Rank.ACE) {
                    aceCount++
                    total += 1  // Contar As como 1 inicialmente
                } else {
                    total += card.rank.value
                }
            }
        }

        // Segunda pasada: convertir Ases de 1 a 11 si no nos pasamos
        while (aceCount > 0 && total + 10 <= 21) {
            total += 10  // Cambiar un As de 1 a 11 (+10)
            aceCount--
        }

        return total
    }

    /**
     * Verifica si la mano es un Blackjack natural
     * (As + carta de valor 10 con solo 2 cartas)
     */
    fun isBlackjack(): Boolean {
        if (cards.size != 2) return false

        val hasAce = cards.any { !it.hidden && it.rank == Rank.ACE }
        val hasTen = cards.any { !it.hidden && it.rank.value == 10 }

        return hasAce && hasTen
    }

    /**
     * Verifica si la mano se ha pasado de 21
     */
    fun isBusted(): Boolean = calculateValue() > 21

    /**
     * Revela todas las cartas ocultas
     */
    fun revealAll() {
        for (i in cards.indices) {
            cards[i] = cards[i].copy(hidden = false)
        }
    }

    /**
     * Número de cartas en la mano
     */
    fun size(): Int = cards.size

    /**
     * Verifica si la mano está vacía
     */
    fun isEmpty(): Boolean = cards.isEmpty()

    override fun toString(): String {
        val cardsStr = cards.joinToString(", ")
        val value = if (cards.all { !it.hidden }) calculateValue().toString() else "?"
        return "Hand(cartas: [$cardsStr], valor: $value)"
    }
}
