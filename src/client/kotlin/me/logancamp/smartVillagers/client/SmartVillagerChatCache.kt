package me.logancamp.smartVillagers.client

import java.util.UUID

object SmartVillagerChatCache {
    private val chats = mutableMapOf<UUID, MutableList<String>>()

    fun getMessages(villagerId: UUID): MutableList<String> {
        return chats.getOrPut(villagerId) { mutableListOf() }
    }

    fun addMessage(villagerId: UUID, message: String) {
        val messages = getMessages(villagerId)
        messages.add(message)
        if (messages.size > 30) {
            messages.removeAt(0)
        }
    }
}