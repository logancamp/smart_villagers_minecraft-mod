package me.logancamp.smartVillagers

import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class VillagerReplyS2CPayload(
    val villagerId: String,
    val reply: String
) : CustomPayload {

    companion object {
        val ID = CustomPayload.Id<VillagerReplyS2CPayload>(
            Identifier.of("smart-villagers", "villager_reply_s2c")
        )

        val CODEC: PacketCodec<RegistryByteBuf, VillagerReplyS2CPayload> = PacketCodec.tuple(
            PacketCodecs.STRING,
            VillagerReplyS2CPayload::villagerId,
            PacketCodecs.STRING,
            VillagerReplyS2CPayload::reply,
            ::VillagerReplyS2CPayload
        )
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}