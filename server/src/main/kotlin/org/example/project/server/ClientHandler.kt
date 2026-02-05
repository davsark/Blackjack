package org.example.project.server

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.model.Deck
import org.example.project.protocol.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*

/**
 * Maneja la comunicación con un cliente individual
 */
class ClientHandler(
    private val socket: Socket,
    private val recordsManager: RecordsManager
) {
    private val playerId = UUID.randomUUID().toString()
    private var playerName: String = "Jugador"
    private lateinit var input: BufferedReader
    private lateinit var output: BufferedWriter
    private val json = Json { ignoreUnknownKeys = true }

    private val deck = Deck()
    private lateinit var dealerAI: DealerAI
    private var gameMode: GameMode? = null

    init {
        deck.shuffle()
    }

    /**
     * Maneja la conexión del cliente
     */
    suspend fun handle() = coroutineScope {
        try {
            // Configurar streams
            input = BufferedReader(InputStreamReader(socket.getInputStream()))
            output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

            // Configurar timeout
            socket.soTimeout = 60_000 // 60 segundos

            println("✅ Cliente conectado: ${socket.inetAddress.hostAddress}:${socket.port}")

            // Loop principal de mensajes
            while (socket.isConnected && !socket.isClosed) {
                val line = try {
                    input.readLine()
                } catch (e: SocketTimeoutException) {
                    println("⏱️ Timeout del cliente $playerName")
                    break
                } catch (e: SocketException) {
                    println("🔌 Conexión cerrada: $playerName")
                    break
                }

                if (line == null) {
                    println("👋 Cliente desconectado: $playerName")
                    break
                }

                try {
                    val message = json.decodeFromString<ClientMessage>(line)
                    handleMessage(message)
                } catch (e: Exception) {
                    println("⚠️ Error al procesar mensaje de $playerName: ${e.message}")
                    sendError("Error al procesar mensaje: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error en ClientHandler para $playerName: ${e.message}")
        } finally {
            cleanup()
        }
    }

    /**
     * Procesa un mensaje del cliente
     */
    private suspend fun handleMessage(message: ClientMessage) {
        when (message) {
            is ClientMessage.JoinGame -> handleJoinGame(message)
            is ClientMessage.RequestCard -> handleRequestCard()
            is ClientMessage.Stand -> handleStand()
            is ClientMessage.NewGame -> handleNewGame()
            is ClientMessage.RequestRecords -> handleRequestRecords()
            is ClientMessage.Ping -> handlePing()
        }
    }

    /**
     * Maneja la unión de un jugador al juego
     */
    private suspend fun handleJoinGame(message: ClientMessage.JoinGame) {
        playerName = message.playerName
        gameMode = message.gameMode

        println("👤 $playerName se une (Modo: ${message.gameMode})")

        when (message.gameMode) {
            GameMode.PVE -> {
                dealerAI = DealerAI(deck)
                sendMessage(ServerMessage.JoinConfirmation(
                    playerId = playerId,
                    message = "Bienvenido $playerName. Modo: Jugador vs Dealer"
                ))
                // Iniciar partida automáticamente
                startPVEGame()
            }
            GameMode.PVP -> {
                sendMessage(ServerMessage.JoinConfirmation(
                    playerId = playerId,
                    message = "Bienvenido $playerName. Modo PVP (En desarrollo)"
                ))
                sendError("Modo PVP aún no implementado completamente. Usa PVE.")
            }
        }
    }

    /**
     * Inicia una nueva partida PVE
     */
    private suspend fun startPVEGame() {
        dealerAI.checkAndResetDeck()
        val gameState = dealerAI.startNewGame(playerId)
        sendMessage(gameState)

        // Verificar si hay Blackjack natural
        if (gameState.playerScore == 21 && gameState.playerHand.size == 2) {
            delay(500) // Pequeña pausa para que el cliente procese el estado
            finishGame()
        }
    }

    /**
     * Maneja la petición de carta
     */
    private suspend fun handleRequestCard() {
        if (gameMode != GameMode.PVE || !::dealerAI.isInitialized) {
            sendError("No hay juego activo")
            return
        }

        val gameState = dealerAI.playerHit(playerId)
        sendMessage(gameState)

        if (gameState.gameState == GamePhase.GAME_OVER) {
            delay(500)
            finishGame()
        }
    }

    /**
     * Maneja cuando el jugador se planta
     */
    private suspend fun handleStand() {
        if (gameMode != GameMode.PVE || !::dealerAI.isInitialized) {
            sendError("No hay juego activo")
            return
        }

        val gameState = dealerAI.playerStand(playerId)
        sendMessage(gameState)

        delay(500)
        finishGame()
    }

    /**
     * Finaliza el juego y envía el resultado
     */
    private suspend fun finishGame() {
        val result = dealerAI.getGameResult(playerId)
        sendMessage(result)

        // Guardar en records
        recordsManager.recordGameResult(playerName, result.result)
    }

    /**
     * Maneja la solicitud de nueva partida
     */
    private suspend fun handleNewGame() {
        if (gameMode == GameMode.PVE && ::dealerAI.isInitialized) {
            startPVEGame()
        } else {
            sendError("Debes unirte primero al juego")
        }
    }

    /**
     * Maneja la solicitud de records
     */
    private suspend fun handleRequestRecords() {
        val records = recordsManager.getTopRecords()
        sendMessage(ServerMessage.RecordsList(records))
    }

    /**
     * Maneja el ping
     */
    private suspend fun handlePing() {
        sendMessage(ServerMessage.Pong)
    }

    /**
     * Envía un mensaje al cliente
     */
    private suspend fun sendMessage(message: ServerMessage) = withContext(Dispatchers.IO) {
        try {
            val jsonMessage = json.encodeToString(message)
            output.write(jsonMessage)
            output.newLine()
            output.flush()
        } catch (e: Exception) {
            println("❌ Error al enviar mensaje a $playerName: ${e.message}")
            throw e
        }
    }

    /**
     * Envía un mensaje de error
     */
    private suspend fun sendError(errorMessage: String) {
        sendMessage(ServerMessage.Error(errorMessage))
    }

    /**
     * Limpieza de recursos
     */
    private fun cleanup() {
        try {
            socket.close()
            println("🧹 Conexión cerrada limpiamente: $playerName")
        } catch (e: Exception) {
            println("⚠️ Error al cerrar conexión: ${e.message}")
        }
    }
}
