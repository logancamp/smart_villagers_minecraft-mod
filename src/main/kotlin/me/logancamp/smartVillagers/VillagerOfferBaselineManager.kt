package me.logancamp.smartVillagers

import net.minecraft.entity.passive.VillagerEntity
import java.util.UUID

object VillagerOfferBaselineManager {

    private data class OfferBaseline(
        val outputCounts: List<Int>
    )

    private val baselines = mutableMapOf<UUID, OfferBaseline>()

    fun ensureBaseline(villager: VillagerEntity) {
        val villagerId = villager.uuid

        if (baselines.containsKey(villagerId)) return

        val offers = villager.offers ?: return

        baselines[villagerId] = OfferBaseline(
            outputCounts = offers.map { it.sellItem.count }
        )
    }

    fun getBaselineOutputCount(villager: VillagerEntity, offerIndex: Int, fallback: Int): Int {
        val baseline = baselines[villager.uuid] ?: return fallback
        return baseline.outputCounts.getOrNull(offerIndex) ?: fallback
    }
}