package org.example.project.server

import kotlinx.coroutines.*
import org.example.project.config.GameConfig
import java.io.File
import java.net.ServerSocket
import java.net.SocketException
import java.util.*

/**
 * Servidor principal del juego de Blackjack
 * Acepta conexiones de clientes y las maneja con corrutinas
 */
class GameServer(private val port: Int = GameConfig.DEFAULT_SERVER_PORT) {
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val recordsManager: RecordsManager
    private var isRunning = false

    init {
        // Cargar configuración
        val config = loadConfiguration()
        val recordsFile = config["server.recordsFile"] ?: "records.json"
        recordsManager = RecordsManager(recordsFile)

        println("📋 Configuración del servidor:")
        config.forEach { (key, value) ->
            println("   $key = $value")
        }
    }

    /**
     * Carga la configuración desde el archivo properties
     */
    private fun loadConfiguration(): Map<String, String> {
        val config = mutableMapOf<String, String>()

        try {
            val configFile = File("server/src/main/resources/server-config.properties")
            if (configFile.exists()) {
                val properties = Properties()
                configFile.inputStream().use { properties.load(it) }
                properties.forEach { key, value ->
                    config[key.toString()] = value.toString()
                }
                println("✅ Configuración cargada desde: ${configFile.absolutePath}")
            } else {
                println("⚠️ No se encontró archivo de configuración, usando valores por defecto")
            }
        } catch (e: Exception) {
            println("⚠️ Error al cargar configuración: ${e.message}")
            println("   Usando valores por defecto")
        }

        return config
    }

    /**
     * Inicia el servidor
     */
    fun start() {
        try {
            serverSocket = ServerSocket(port)
            isRunning = true

            println()
            println("=" .repeat(60))
            println("🎰 SERVIDOR DE BLACKJACK INICIADO")
            println("=" .repeat(60))
            println("📡 Puerto: $port")
            println("🎮 Esperando conexiones de clientes...")
            println("🛑 Presiona Ctrl+C para detener el servidor")
            println("=" .repeat(60))
            println()

            // Loop principal de aceptación de conexiones
            while (isRunning) {
                try {
                    val clientSocket = serverSocket?.accept() ?: break

                    println("🔔 Nueva conexión desde: ${clientSocket.inetAddress.hostAddress}:${clientSocket.port}")

                    // Lanzar una corrutina para manejar este cliente
                    scope.launch {
                        val handler = ClientHandler(clientSocket, recordsManager)
                        try {
                            handler.handle()
                        } catch (e: Exception) {
                            println("❌ Error manejando cliente: ${e.message}")
                        }
                    }
                } catch (e: SocketException) {
                    if (isRunning) {
                        println("⚠️ Error en socket: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error fatal en servidor: ${e.message}")
            e.printStackTrace()
        } finally {
            stop()
        }
    }

    /**
     * Detiene el servidor
     */
    fun stop() {
        println()
        println("🛑 Deteniendo servidor...")
        isRunning = false

        try {
            serverSocket?.close()
            scope.cancel()
            println("✅ Servidor detenido correctamente")
        } catch (e: Exception) {
            println("⚠️ Error al detener servidor: ${e.message}")
        }
    }
}

/**
 * Punto de entrada del servidor
 */
fun main() {
    val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: GameConfig.DEFAULT_SERVER_PORT
    val server = GameServer(port)

    // Manejar cierre graceful
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop()
    })

    server.start()
}
