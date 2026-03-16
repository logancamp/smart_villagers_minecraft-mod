package me.logancamp.smartVillagers.client.mixin

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import net.minecraft.client.gui.screen.ingame.MerchantScreen

@Mixin(MerchantScreen::class)
abstract class MerchantScreenMixin {

    @Shadow
    protected lateinit var client: MinecraftClient

    @Shadow
    protected lateinit var textRenderer: TextRenderer

    @Shadow
    protected var x: Int = 0

    @Shadow
    protected var y: Int = 0

    @Shadow
    protected var backgroundWidth: Int = 0

    @Shadow
    protected var backgroundHeight: Int = 0

    @Shadow
    protected abstract fun <T> addDrawableChild(drawableElement: T): T
            where T : Element, T : Drawable, T : Selectable

    private var chatInput: TextFieldWidget? = null
    private val messages = mutableListOf<String>()

    @Inject(method = ["init()V"], at = [At("TAIL")])
    private fun afterInit(ci: CallbackInfo) {
        val chatX = x + backgroundWidth + 6
        val chatY = y + backgroundHeight - 18

        chatInput = TextFieldWidget(
            textRenderer,
            chatX,
            chatY,
            120,
            12,
            Text.literal("")
        )

        chatInput!!.setMaxLength(256)
        chatInput!!.setDrawsBackground(false)

        addDrawableChild(chatInput!!)

        if (messages.isEmpty()) {
            messages.add("Villager: Hello.")
        }

        println("MerchantScreenMixin input added")
    }
}