package me.logancamp.smartVillagers

sealed class VillagerAction {
    data class AdjustPrice(val amount: Int) : VillagerAction()
    data class AdjustOutput(val amount: Int) : VillagerAction()
}