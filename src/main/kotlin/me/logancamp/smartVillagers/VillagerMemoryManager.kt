package me.logancamp.smartVillagers

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

object VillagerMemoryManager {

    private data class SavedVillagerMemory(
        val villagerId: String,
        val playerId: String,
        val relationship: Int,
        val mood: String,
        val bargainingPressure: Int,
        val currentPriceModifier: Int,
        val currentOutputModifier: Int,
        val recentMessages: List<String>
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val savePath: Path = Paths.get("smart-villagers-memory.json")

    private val memories = mutableMapOf<Pair<UUID, String>, VillagerMemory>()

    init {
        load()
    }

    fun getMemory(villagerId: UUID, playerId: String): VillagerMemory {
        val key = Pair(villagerId, playerId)
        return memories.getOrPut(key) {
            VillagerMemory(villagerId, playerId)
        }
    }

    fun addMessage(villagerId: UUID, playerId: String, message: String) {
        val memory = getMemory(villagerId, playerId)

        memory.recentMessages.add(message)

        if (memory.recentMessages.size > 10) {
            memory.recentMessages.removeAt(0)
        }

        save()
    }

    fun saveMemory(memory: VillagerMemory) {
        memories[Pair(memory.villagerId, memory.playerId)] = memory
        save()
    }

    private fun save() {
        val saved = memories.values.map {
            SavedVillagerMemory(
                villagerId = it.villagerId.toString(),
                playerId = it.playerId,
                relationship = it.relationship,
                mood = it.mood,
                bargainingPressure = it.bargainingPressure,
                currentPriceModifier = it.currentPriceModifier,
                currentOutputModifier = it.currentOutputModifier,
                recentMessages = it.recentMessages.toList()
            )
        }

        Files.writeString(savePath, gson.toJson(saved))
    }

    private fun load() {
        if (!Files.exists(savePath)) return

        val json = Files.readString(savePath)
        val array = gson.fromJson(json, Array<SavedVillagerMemory>::class.java) ?: return

        memories.clear()

        for (saved in array) {
            try {
                val villagerId = UUID.fromString(saved.villagerId)

                memories[Pair(villagerId, saved.playerId)] = VillagerMemory(
                    villagerId = villagerId,
                    playerId = saved.playerId,
                    relationship = saved.relationship,
                    mood = saved.mood,
                    bargainingPressure = saved.bargainingPressure,
                    currentPriceModifier = saved.currentPriceModifier,
                    currentOutputModifier = saved.currentOutputModifier,
                    recentMessages = saved.recentMessages.toMutableList()
                )
            } catch (_: IllegalArgumentException) {
            }
        }
    }
}