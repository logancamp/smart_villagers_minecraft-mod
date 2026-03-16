package me.logancamp.smartVillagers.client

import me.logancamp.smartVillagers.SmartVillagersClientState
import me.logancamp.smartVillagers.VillagerChatC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.MerchantScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import java.util.UUID

class SmartVillagerScreen(
    handler: MerchantScreenHandler,
    inventory: PlayerInventory,
    title: Text
) : MerchantScreen(handler, inventory, title) {

    companion object {
        private var currentInstance: SmartVillagerScreen? = null

        fun receiveVillagerReply(villagerId: String, reply: String) {
            val uuid = try {
                UUID.fromString(villagerId)
            } catch (_: IllegalArgumentException) {
                return
            }

            SmartVillagerChatCache.addMessage(uuid, "Villager: $reply")

            val screen = currentInstance ?: return
            val currentVillager = screen.currentVillagerId() ?: return

            if (currentVillager == uuid) {
                screen.waitingForReply = false
                screen.pendingReply = null
                screen.replyReadyAtMs = 0L
                screen.messages.clear()
                screen.messages.addAll(SmartVillagerChatCache.getMessages(uuid))
                screen.chatScroll = 0
            }
        }
    }

    private lateinit var chatInput: TextFieldWidget
    private val messages = mutableListOf<String>()
    private var chatScroll = 0

    private var waitingForReply = false
    private var pendingReply: String? = null
    private var replyReadyAtMs = 0L
    private val minimumReplyDelayMs = 1200L

    private val chatPanelWidth = 132
    private val vanillaMerchantWidth = 276

    private fun chatLeft(): Int = x + vanillaMerchantWidth + 6
    private fun chatTop(): Int = y + 8
    private fun chatWidth(): Int = chatPanelWidth - 12
    private fun chatHeight(): Int = backgroundHeight - 34

    override fun init() {
        backgroundWidth = vanillaMerchantWidth + chatPanelWidth
        super.init()

        currentInstance = this

        chatInput = TextFieldWidget(
            textRenderer,
            chatLeft(),
            y + backgroundHeight - 18,
            chatWidth(),
            12,
            Text.literal("")
        )

        chatInput.setMaxLength(256)
        chatInput.setDrawsBackground(false)
        chatInput.isFocused = true
        chatInput.setEditable(true)

        addDrawableChild(chatInput)

        messages.clear()

        val villagerId = currentVillagerId()
        if (villagerId != null) {
            messages.addAll(SmartVillagerChatCache.getMessages(villagerId))
        }

        if (messages.isEmpty()) {
            messages.add("Villager: Hello.")
            messages.add("Villager: What do you need?")
        }
    }

    override fun close() {
        if (currentInstance === this) {
            currentInstance = null
        }
        super.close()
    }

    override fun keyPressed(input: KeyInput): Boolean {
        if (chatInput.isFocused) {
            if (client != null && client!!.options.inventoryKey.matchesKey(input)) {
                return true
            }

            if (input.keycode == 257 || input.keycode == 335) {
                if (waitingForReply) {
                    return true
                }

                val raw = chatInput.text.trim()

                if (raw.isNotBlank()) {
                    val villagerId = currentVillagerId()

                    if (villagerId != null) {
                        SmartVillagerChatCache.addMessage(villagerId, "You: $raw")
                        messages.clear()
                        messages.addAll(SmartVillagerChatCache.getMessages(villagerId))

                        waitingForReply = true
                        pendingReply = null
                        replyReadyAtMs = System.currentTimeMillis() + minimumReplyDelayMs

                        ClientPlayNetworking.send(
                            VillagerChatC2SPayload(
                                villagerId = villagerId.toString(),
                                message = raw
                            )
                        )
                    } else {
                        messages.add("Villager: ...")
                    }

                    chatInput.text = ""
                    chatScroll = 0
                }

                return true
            }

            if (chatInput.keyPressed(input)) {
                return true
            }
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharInput): Boolean {
        if (chatInput.isFocused && chatInput.charTyped(input)) {
            return true
        }
        return super.charTyped(input)
    }

    private fun currentVillagerId(): UUID? {
        return SmartVillagersClientState.lastInteractedVillagerId
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        if (mouseX >= chatLeft() && mouseX <= chatLeft() + chatWidth()) {
            val wrappedLines = buildWrappedLines()
            val maxVisibleLines = chatHeight() / 10
            val maxScroll = (wrappedLines.size - maxVisibleLines).coerceAtLeast(0)

            if (verticalAmount > 0) {
                chatScroll = (chatScroll + 1).coerceAtMost(maxScroll)
                return true
            }

            if (verticalAmount < 0) {
                chatScroll = (chatScroll - 1).coerceAtLeast(0)
                return true
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun typingIndicatorText(): String {
        val dots = ((System.currentTimeMillis() / 350L) % 4).toInt()
        return "Villager is thinking" + ".".repeat(dots)
    }

    private fun flushPendingReplyIfReady() {
        if (!waitingForReply) return
        if (System.currentTimeMillis() < replyReadyAtMs) return

        val villagerId = currentVillagerId() ?: return
        messages.clear()
        messages.addAll(SmartVillagerChatCache.getMessages(villagerId))
    }

    private fun buildWrappedLines(): List<Pair<OrderedText, Int>> {
        val lines = mutableListOf<Pair<OrderedText, Int>>()

        for (message in messages) {
            val color = when {
                message.startsWith("You:") -> 0xFF9CDCFE.toInt()
                message.startsWith("Villager:") -> 0xFFB5CEA8.toInt()
                else -> 0xFFFFFFFF.toInt()
            }

            val wrapped = textRenderer.wrapLines(Text.literal(message), chatWidth() - 4)
            for (line in wrapped) {
                lines.add(line to color)
            }
        }

        if (waitingForReply) {
            val wrapped = textRenderer.wrapLines(Text.literal(typingIndicatorText()), chatWidth() - 4)
            for (line in wrapped) {
                lines.add(line to 0xFFCE9178.toInt())
            }
        }

        return lines
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.drawBackground(context, delta, mouseX, mouseY)

        context.fill(
            x + vanillaMerchantWidth,
            y,
            x + vanillaMerchantWidth + chatPanelWidth,
            y + backgroundHeight,
            0x10000000
        )

        context.fill(
            chatLeft(),
            chatTop(),
            chatLeft() + chatWidth(),
            chatTop() + chatHeight(),
            0x0A000000
        )

        context.drawVerticalLine(
            x + vanillaMerchantWidth,
            y + 6,
            y + backgroundHeight - 6,
            0x40FFFFFF
        )
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        val labelColor = 0xFF404040.toInt()

        val merchantTitle = if (handler.levelProgress in 1..5) {
            Text.literal("${title.string} - ${Text.translatable("merchant.level.${handler.levelProgress}").string}")
        } else {
            title
        }

        val xpBarX = (backgroundWidth - 102) / 2
        val centeredTitleX = xpBarX + 51 - (textRenderer.getWidth(merchantTitle) / 2)

        context.drawText(textRenderer, Text.literal("Trades"), 5, 6, labelColor, false)
        context.drawText(textRenderer, merchantTitle, centeredTitleX, 6, labelColor, false)
        context.drawText(textRenderer, playerInventoryTitle, 107, backgroundHeight - 94, labelColor, false)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        flushPendingReplyIfReady()
        super.render(context, mouseX, mouseY, delta)
        drawChatLog(context)
    }

    private fun drawChatLog(context: DrawContext) {
        val wrappedLines = buildWrappedLines()
        val maxVisibleLines = chatHeight() / 10
        val totalLines = wrappedLines.size

        val startIndex = (totalLines - maxVisibleLines - chatScroll).coerceAtLeast(0)
        val endIndex = (startIndex + maxVisibleLines).coerceAtMost(totalLines)
        val visible = wrappedLines.subList(startIndex, endIndex)

        var drawY = chatTop() + chatHeight() - (visible.size * 10) - 2

        for ((line, color) in visible) {
            context.drawText(textRenderer, line, chatLeft() + 6, drawY, color, false)
            drawY += 10
        }
    }
}