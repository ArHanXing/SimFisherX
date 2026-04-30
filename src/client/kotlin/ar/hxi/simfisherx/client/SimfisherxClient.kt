package ar.hxi.simfisherx.client  // 按需改成 ar.hxi.simfisherx.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Items

class SimfisherxClient : ClientModInitializer {
    
    private val antiAFKHandler = AntiAFKHandler()
    private var fishingCooldown = 0
    private val FISHING_CD_DURATION = 100 // 5秒 = 100 ticks
    
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client -> onTick(client) }
        println("[SFX] loaded simfisherx / if you dont want this logged, remove every line contains [SFX]")
    }

    private fun isHoldingFishingRodInEitherHand(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        return player.mainHandStack.item == Items.FISHING_ROD ||
                player.offHandStack.item == Items.FISHING_ROD
    }

    private fun onTick(client: MinecraftClient) {
        handleFishingLogic(client)
        antiAFKHandler.onTick(client)
    }
    
    private fun handleFishingLogic(client: MinecraftClient) {
        if (fishingCooldown > 0) {
            fishingCooldown--
            return
        }
        
        val player = client.player ?: return
        val currentTitle = Utils.getCurrentTitle() ?: return
        val titleStr = currentTitle.string

        var flag = false
        var i = 0
        while (i < titleStr.length) {
            val codePoint = titleStr.codePointAt(i)
            if (codePoint == "🐟".codePointAt(0)) {

                flag = true
                var j = i + Character.charCount(codePoint)
                while (j < titleStr.length) {
                    val nextCodePoint = titleStr.codePointAt(j)
                    if (nextCodePoint == "◆".codePointAt(0)) {
                        flag = false
                        print("[SFX] Click! now title \"$currentTitle\" ,finding fish logo at $i");
                        break
                    }
                    j += Character.charCount(nextCodePoint)
                }
                if (!flag) break
            }
            i += Character.charCount(codePoint)
        }

        if (flag) {
            if (isHoldingFishingRodInEitherHand()) {
                client.interactionManager?.interactItem(player, player.activeHand)
                fishingCooldown = FISHING_CD_DURATION
                println("[SFX] Fishing rod reeled in, starting 5s cooldown")
            }
        }
    }
}