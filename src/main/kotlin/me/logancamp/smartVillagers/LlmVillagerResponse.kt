package me.logancamp.smartVillagers

data class LlmVillagerResponse(
    val reply: String,
    val mood: String? = null,
    val relationshipDelta: Int? = null,
    val bargainingPressureDelta: Int? = null,
    val actions: List<LlmAction> = emptyList()
)

data class LlmAction(
    val type: String,
    val amount: Int? = null
)