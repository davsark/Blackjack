package org.example.project.config

/**
 * Configuración del juego
 *
 * Contiene constantes y parámetros configurables
 */
object GameConfig {
    /**
     * Puerto por defecto del servidor
     */
    const val DEFAULT_SERVER_PORT = 9999

    /**
     * Host por defecto del servidor
     */
    const val DEFAULT_SERVER_HOST = "localhost"

    /**
     * Número máximo de jugadores por mesa (PVP)
     */
    const val MAX_PLAYERS_PER_TABLE = 4

    /**
     * Timeout de conexión en milisegundos
     */
    const val CONNECTION_TIMEOUT_MS = 60_000L

    /**
     * Intervalo de ping en milisegundos
     */
    const val PING_INTERVAL_MS = 30_000L

    /**
     * Número de cartas restantes en la baraja para resetear
     */
    const val DECK_RESET_THRESHOLD = 15

    /**
     * Número máximo de records a guardar
     */
    const val MAX_RECORDS = 100

    /**
     * Número de records a mostrar en el top
     */
    const val TOP_RECORDS_DISPLAY = 10
}
