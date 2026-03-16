package me.logancamp.smartVillagers

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.passive.VillagerEntity
import java.util.UUID
import java.util.concurrent.Executors

class SmartVillagers : ModInitializer {

    companion object {
        val LOGGER = org.slf4j.LoggerFactory.getLogger("smart-villagers")
        private val llmExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onInitialize() {
        ModScreenHandlers.register()
        VillagerInteraction.register()

        PayloadTypeRegistry.playC2S().register(
            VillagerChatC2SPayload.ID,
            VillagerChatC2SPayload.CODEC
        )

        PayloadTypeRegistry.playS2C().register(
            VillagerReplyS2CPayload.ID,
            VillagerReplyS2CPayload.CODEC
        )

        ServerPlayNetworking.registerGlobalReceiver(VillagerChatC2SPayload.ID) { payload, context ->
            val player = context.player()

            val villagerUuid = try {
                UUID.fromString(payload.villagerId)
            } catch (_: IllegalArgumentException) {
                return@registerGlobalReceiver
            }

            llmExecutor.execute {
                val playerId = player.uuid.toString()

                val response = VillagerConversationManager.handlePlayerMessage(
                    villagerUuid,
                    playerId,
                    payload.message
                )

                context.server().execute {
                    val villager = player.entityWorld.getEntity(villagerUuid) as? VillagerEntity
                        ?: return@execute

                    val changedOffers = response.actions.any {
                        (it is VillagerAction.AdjustPrice && it.amount != 0) ||
                                (it is VillagerAction.AdjustOutput && it.amount != 0)
                    }

                    ServerPlayNetworking.send(
                        player,
                        VillagerReplyS2CPayload(
                            villagerId = villagerUuid.toString(),
                            reply = response.reply
                        )
                    )
                }
            }
        }
    }
}