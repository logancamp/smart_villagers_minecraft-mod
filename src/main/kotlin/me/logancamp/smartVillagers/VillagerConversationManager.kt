package me.logancamp.smartVillagers

import java.util.UUID

object VillagerConversationManager {

    fun handlePlayerMessage(villagerId: UUID, playerId: String, message: String): VillagerResponse {
        VillagerMemoryManager.addMessage(villagerId, playerId, "Player: $message")

        val memory = VillagerMemoryManager.getMemory(villagerId, playerId)

        val prompt = VillagerPromptBuilder.buildPrompt(
            profession = "villager",
            level = 1,
            memory = memory,
            playerMessage = message
        )

        val llmResponse = LlmClient.sendPrompt(prompt)
        println("LLM response: $llmResponse")
        println("PARSED LLM RESPONSE: $llmResponse")

        val response = if (llmResponse != null) {
            val relationshipDelta = (llmResponse.relationshipDelta ?: 0).coerceIn(-2, 2)
            val pressureDelta = (llmResponse.bargainingPressureDelta ?: 0).coerceIn(-1, 2)

            memory.relationship += relationshipDelta
            memory.bargainingPressure =
                (memory.bargainingPressure + pressureDelta).coerceAtLeast(0)

            memory.mood = when (llmResponse.mood?.lowercase()) {
                "friendly" -> "friendly"
                "neutral" -> "neutral"
                "cautious" -> "cautious"
                "hostile" -> "hostile"
                else -> memory.mood
            }

            val validatedActions = llmResponse.actions.mapNotNull { action ->
                when (action.type.lowercase()) {
                    "adjust_price" -> {
                        val requested = (action.amount ?: 0).coerceIn(-1, 1)

                        if (requested != 0) {
                            memory.currentPriceModifier += requested
                            VillagerAction.AdjustPrice(requested)
                        } else {
                            null
                        }
                    }

                    "adjust_output" -> {
                        val requested = (action.amount ?: 0).coerceIn(-1, 1)

                        if (requested != 0) {
                            memory.currentOutputModifier += requested
                            VillagerAction.AdjustOutput(requested)
                        } else {
                            null
                        }
                    }

                    else -> null
                }
            }

            VillagerResponse(
                reply = rewriteReplyIfNeeded(memory, llmResponse.reply, validatedActions),
                actions = validatedActions
            )
        } else {
            fallbackResponse(memory)
        }

        VillagerMemoryManager.addMessage(villagerId, playerId, "Villager: ${response.reply}")

        println("VALIDATED ACTIONS: ${response.actions}")
        println("MEMORY priceModifier=${memory.currentPriceModifier} outputModifier=${memory.currentOutputModifier}")

        VillagerMemoryManager.saveMemory(memory)

        return response
    }

    private fun rewriteReplyIfNeeded(
        memory: VillagerMemory,
        originalReply: String,
        actions: List<VillagerAction>
    ): String {
        val priceRaised = actions.any { it is VillagerAction.AdjustPrice && it.amount > 0 }
        val priceLowered = actions.any { it is VillagerAction.AdjustPrice && it.amount < 0 }
        val outputChanged = actions.any { it is VillagerAction.AdjustOutput && it.amount != 0 }
        val anyOfferChanged = priceRaised || priceLowered || outputChanged

        val baseReply = when {
            priceRaised && memory.mood == "hostile" -> "Watch your tongue. Prices just went up."
            priceRaised -> "Push again and it will cost you more."
            priceLowered -> "Very well. I can lower it a little."
            memory.bargainingPressure >= 3 && memory.mood != "friendly" ->
                "Enough. I will not lower it."
            !priceLowered && soundsLikeDiscountPromise(originalReply) ->
                "No. I will not lower it."
            else -> originalReply
        }

        return if (anyOfferChanged) {
            "$baseReply\nCome back in a second and I'll update my prices."
        } else {
            baseReply
        }
    }

    private fun soundsLikeDiscountPromise(reply: String): Boolean {
        val lower = reply.lowercase()

        return listOf(
            "lower the price",
            "lower it",
            "discount",
            "cheaper",
            "for you, maybe",
            "i can lower",
            "i might lower",
            "perhaps i can lower",
            "maybe i can lower",
            "i will lower",
            "i can make it cheaper"
        ).any { it in lower }
    }

    private fun fallbackResponse(memory: VillagerMemory): VillagerResponse {
        val actions = mutableListOf<VillagerAction>()

        var reply = when {
            memory.mood == "hostile" && memory.bargainingPressure >= 4 -> {
                memory.currentPriceModifier += 1
                actions.add(VillagerAction.AdjustPrice(1))
                "You push too much. Prices just went up."
            }

            memory.relationship >= 3 && memory.mood == "friendly" && memory.bargainingPressure <= 2 -> {
                memory.currentPriceModifier -= 1
                actions.add(VillagerAction.AdjustPrice(-1))
                "For you, maybe."
            }

            memory.relationship >= 5 -> "You seem familiar."
            memory.relationship <= -3 -> "I do not trust you."
            memory.mood == "friendly" -> "Hmm, yes?"
            memory.mood == "hostile" -> "What do you want?"
            memory.mood == "cautious" -> "Prices depend on trust."
            else -> "Hmm."
        }

        return VillagerResponse(
            reply = reply,
            actions = actions
        )
    }
}