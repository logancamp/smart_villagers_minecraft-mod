package me.logancamp.smartVillagers

import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.util.ActionResult
import me.logancamp.smartVillagers.SmartVillagersClientState

object VillagerInteraction {

    fun register() {
        UseEntityCallback.EVENT.register(UseEntityCallback { player, world, hand, entity, hitResult ->

            if (world.isClient) {
                return@UseEntityCallback ActionResult.PASS
            }

            if (entity !is VillagerEntity) {
                return@UseEntityCallback ActionResult.PASS
            }

            SmartVillagers.LOGGER.info("Villager confirmed")
            SmartVillagers.LOGGER.info("Clicked villager: id=${entity.id}")
            SmartVillagersClientState.lastInteractedVillagerId = entity.uuid

            val serverPlayer = player as? net.minecraft.server.network.ServerPlayerEntity
            if (serverPlayer != null) {
                val playerId = serverPlayer.uuid.toString()
                val memory = VillagerMemoryManager.getMemory(entity.uuid, playerId)

                VillagerActionApplier.applyPersistentModifiers(
                    entity,
                    memory.currentPriceModifier,
                    memory.currentOutputModifier
                )
            }

            ActionResult.PASS
        })
    }
}