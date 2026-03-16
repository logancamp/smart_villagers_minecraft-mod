package me.logancamp.smartVillagers

import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class VillagerChatC2SPayload(
    val villagerId: String,
    val message: String
) : CustomPayload {

    companion object {
        val ID = CustomPayload.Id<VillagerChatC2SPayload>(
            Identifier.of("smart-villagers", "villager_chat_c2s")
        )

        val CODEC: PacketCodec<RegistryByteBuf, VillagerChatC2SPayload> = PacketCodec.tuple(
            PacketCodecs.STRING,
            VillagerChatC2SPayload::villagerId,
            PacketCodecs.STRING,
            VillagerChatC2SPayload::message,
            ::VillagerChatC2SPayload
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}