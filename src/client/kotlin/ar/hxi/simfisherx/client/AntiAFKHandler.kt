package ar.hxi.simfisherx.client

import net.minecraft.client.MinecraftClient
import net.minecraft.item.Items
import kotlin.random.Random

class AntiAFKHandler {

    private var tickCounter = 0
    private val INTERVAL = 1200 // 1分钟 = 1200 ticks（测试用）

    private val actions = listOf<((MinecraftClient) -> Unit)>(
        { client -> ActionExecutor.moveSideways(client) },
        { client -> ActionExecutor.jump(client) },
        { client -> ActionExecutor.rotateHead(client) }
    )

    fun onTick(client: MinecraftClient) {
        if (ActionExecutor.isExecutingRotation()) {
            ActionExecutor.rotateHead(client)
            return
        }

        tickCounter++

        if (tickCounter >= INTERVAL) {
            if (!isHoldingFishingRod(client)) {
                println("[SFX] AntiAFK skipped - not holding fishing rod")
                tickCounter = 0
                return
            }
            
            tickCounter = 0
            executeRandomActions(client)
        }
    }

    private fun isHoldingFishingRod(client: MinecraftClient): Boolean {
        val player = client.player ?: return false
        return player.mainHandStack.item == Items.FISHING_ROD ||
                player.offHandStack.item == Items.FISHING_ROD
    }

    private fun executeRandomActions(client: MinecraftClient) {
        println("[SFX] AntiAFK triggered! Selecting 2 random actions from 3")

        val selectedActions = actions.shuffled(Random).take(2)

        selectedActions.forEach { action ->
            action(client)
        }
    }

    fun reset() {
        tickCounter = 0
    }
}
