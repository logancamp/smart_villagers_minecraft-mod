package me.logancamp.smartVillagers

import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.village.TradeOffer

object VillagerActionApplier {

    fun applyPersistentModifiers(
        villager: VillagerEntity,
        priceModifier: Int,
        outputModifier: Int
    ) {
        val offers = villager.offers ?: return

        VillagerOfferBaselineManager.ensureBaseline(villager)

        for (i in 0 until offers.size) {
            val oldOffer = offers[i]

            val rebuilt = rebuildOffer(
                villager = villager,
                offerIndex = i,
                oldOffer = oldOffer,
                priceModifier = priceModifier,
                outputModifier = outputModifier
            )

            offers[i] = rebuilt
        }

        println("OFFERS REPLACED")
    }

    private fun rebuildOffer(
        villager: VillagerEntity,
        offerIndex: Int,
        oldOffer: TradeOffer,
        priceModifier: Int,
        outputModifier: Int
    ): TradeOffer {
        val newSellStack = oldOffer.sellItem.copy()

        val baselineOutput = VillagerOfferBaselineManager.getBaselineOutputCount(
            villager,
            offerIndex,
            newSellStack.count
        )

        newSellStack.count = (baselineOutput + outputModifier).coerceIn(1, newSellStack.maxCount)

        val rebuilt = TradeOffer(
            oldOffer.firstBuyItem,
            oldOffer.secondBuyItem,
            newSellStack,
            oldOffer.uses,
            oldOffer.maxUses,
            oldOffer.merchantExperience,
            oldOffer.priceMultiplier,
            oldOffer.demandBonus
        )

        rebuilt.setSpecialPrice(priceModifier.coerceIn(-8, 8))
        return rebuilt
    }
}