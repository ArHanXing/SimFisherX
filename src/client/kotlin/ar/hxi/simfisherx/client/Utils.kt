package ar.hxi.simfisherx.client  // 保持与 mixin 包一致

import ar.hxi.simfisherx.client.mixins.InGameHudAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object Utils {
    fun getCurrentTitle(): Text? {
        val accessor = MinecraftClient.getInstance().inGameHud as InGameHudAccessor
        return if (accessor.titleRemainTicks > 0) accessor.title else null
    }

    fun getCurrentSubTitle(): Text? {
        val accessor = MinecraftClient.getInstance().inGameHud as InGameHudAccessor
        return if (accessor.titleRemainTicks > 0) accessor.subtitle else null
    }
}