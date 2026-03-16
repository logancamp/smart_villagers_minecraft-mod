package me.logancamp.smartVillagers

import java.util.UUID

data class VillagerMemory(
    val villagerId: UUID,
    val playerId: String,
    var relationship: Int = 0,
    var mood: String = "neutral",
    var bargainingPressure: Int = 0,
    var currentPriceModifier: Int = 0,
    var currentOutputModifier: Int = 0,
    val recentMessages: MutableList<String> = mutableListOf()
)