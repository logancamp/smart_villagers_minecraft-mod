package me.logancamp.smartVillagers.client

import me.logancamp.smartVillagers.VillagerReplyS2CPayload
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.screen.ingame.MerchantScreen

class SmartVillagersClient : ClientModInitializer {

    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(VillagerReplyS2CPayload.ID) { payload, context ->
            context.client().execute {
                SmartVillagerScreen.receiveVillagerReply(payload.villagerId, payload.reply)
            }
        }

        ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
            if (screen is MerchantScreen && screen !is SmartVillagerScreen) {
                val handler = screen.screenHandler

                client.execute {
                    if (client.currentScreen === screen) {
                        client.setScreen(
                            SmartVillagerScreen(
                                handler,
                                client.player!!.inventory,
                                screen.title
                            )
                        )
                    }
                }
            }
        }
    }
}