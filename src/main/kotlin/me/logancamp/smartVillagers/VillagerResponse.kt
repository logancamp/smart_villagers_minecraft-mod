package me.logancamp.smartVillagers

data class VillagerResponse(
    val reply: String,
    val actions: List<VillagerAction> = emptyList()
)