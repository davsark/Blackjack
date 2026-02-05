package org.example.project.server

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.config.GameConfig
import org.example.project.protocol.GameResultType
import org.example.project.protocol.Record
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Gestor de records del juego
 * Guarda y lee records en formato JSON
 */
class RecordsManager(private val recordsFilePath: String = "records.json") {
    private val recordsFile = File(recordsFilePath)
    private val records = mutableMapOf<String, PlayerStats>()
    private val lock = ReentrantReadWriteLock()
    private val json = Json { prettyPrint = true }

    data class PlayerStats(
        val playerName: String,
        var wins: Int = 0,
        var losses: Int = 0,
        var blackjacks: Int = 0,
        var lastPlayed: Long = System.currentTimeMillis()
    )

    init {
        loadRecords()
    }

    /**
     * Carga los records desde el archivo JSON
     */
    private fun loadRecords() = lock.write {
        try {
            if (recordsFile.exists()) {
                val jsonContent = recordsFile.readText()
                val loadedRecords = json.decodeFromString<List<Record>>(jsonContent)
                loadedRecords.forEach { record ->
                    records[record.playerName] = PlayerStats(
                        playerName = record.playerName,
                        wins = record.wins,
                        losses = record.losses,
                        blackjacks = record.blackjacks,
                        lastPlayed = record.timestamp
                    )
                }
                println("✅ Records cargados: ${records.size} jugadores")
            } else {
                println("ℹ️ No hay archivo de records previo, se creará uno nuevo")
            }
        } catch (e: Exception) {
            println("⚠️ Error al cargar records: ${e.message}")
            println("   Se iniciará con records vacíos")
        }
    }

    /**
     * Guarda los records en el archivo JSON
     */
    private fun saveRecords() {
        try {
            val recordsList = records.values.map { stats ->
                Record(
                    playerName = stats.playerName,
                    wins = stats.wins,
                    losses = stats.losses,
                    blackjacks = stats.blackjacks,
                    timestamp = stats.lastPlayed
                )
            }.sortedByDescending { it.wins }
                .take(GameConfig.MAX_RECORDS)

            val jsonContent = json.encodeToString(recordsList)
            recordsFile.writeText(jsonContent)
            println("💾 Records guardados: ${recordsList.size} jugadores")
        } catch (e: Exception) {
            println("❌ Error al guardar records: ${e.message}")
        }
    }

    /**
     * Registra el resultado de una partida
     */
    fun recordGameResult(playerName: String, result: GameResultType) = lock.write {
        val stats = records.getOrPut(playerName) {
            PlayerStats(playerName = playerName)
        }

        when (result) {
            GameResultType.WIN -> stats.wins++
            GameResultType.LOSE -> stats.losses++
            GameResultType.PUSH -> { /* Empate, no cuenta */ }
            GameResultType.BLACKJACK -> {
                stats.wins++
                stats.blackjacks++
            }
        }

        stats.lastPlayed = System.currentTimeMillis()
        saveRecords()

        println("📊 Record actualizado: $playerName (${stats.wins}W-${stats.losses}L, ${stats.blackjacks} BJ)")
    }

    /**
     * Obtiene el top de records
     */
    fun getTopRecords(limit: Int = GameConfig.TOP_RECORDS_DISPLAY): List<Record> = lock.read {
        records.values
            .map { stats ->
                Record(
                    playerName = stats.playerName,
                    wins = stats.wins,
                    losses = stats.losses,
                    blackjacks = stats.blackjacks,
                    timestamp = stats.lastPlayed
                )
            }
            .sortedWith(compareByDescending<Record> { it.wins }.thenBy { it.losses })
            .take(limit)
    }

    /**
     * Obtiene las estadísticas de un jugador específico
     */
    fun getPlayerStats(playerName: String): Record? = lock.read {
        records[playerName]?.let { stats ->
            Record(
                playerName = stats.playerName,
                wins = stats.wins,
                losses = stats.losses,
                blackjacks = stats.blackjacks,
                timestamp = stats.lastPlayed
            )
        }
    }
}
