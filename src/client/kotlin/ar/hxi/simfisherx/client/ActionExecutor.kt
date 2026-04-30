package ar.hxi.simfisherx.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import kotlin.random.Random

object ActionExecutor {
    
    private var originalYaw: Float = 0f
    private var targetYaw: Float = 0f
    private var isRotating: Boolean = false
    private var rotationPhase: RotationPhase = RotationPhase.IDLE
    private var rotationTimer: Int = 0
    private val ROTATION_DURATION = 40 // 转头持续40 ticks（2秒）
    
    enum class RotationPhase {
        IDLE,
        ROTATING_AWAY,
        HOLDING,
        ROTATING_BACK
    }
    
    /**
     * 左右移动一下 - 纯客户端方式：模拟按键（会回到原位）
     */
    fun moveSideways(client: MinecraftClient) {
        val player = client.player ?: return
        
        println("[SFX] Executing: Move sideways")
        
        val direction = if (Random.nextBoolean()) 1 else -1
        
        client.execute {
            kotlin.runCatching {
                if (direction > 0) {
                    KeyBinding.setKeyPressed(client.options.rightKey.defaultKey, true)
                    Thread.sleep(200)
                    KeyBinding.setKeyPressed(client.options.rightKey.defaultKey, false)
                    
                    Thread.sleep(500)
                    
                    KeyBinding.setKeyPressed(client.options.leftKey.defaultKey, true)
                    Thread.sleep(200)
                    KeyBinding.setKeyPressed(client.options.leftKey.defaultKey, false)
                } else {
                    KeyBinding.setKeyPressed(client.options.leftKey.defaultKey, true)
                    Thread.sleep(200)
                    KeyBinding.setKeyPressed(client.options.leftKey.defaultKey, false)
                    
                    Thread.sleep(500)
                    
                    KeyBinding.setKeyPressed(client.options.rightKey.defaultKey, true)
                    Thread.sleep(200)
                    KeyBinding.setKeyPressed(client.options.rightKey.defaultKey, false)
                }
                println("[SFX] Move sideways completed, returned to original position")
            }
        }
    }

    /**
     * 跳跃一下 - 纯客户端方式：模拟按键
     */
    fun jump(client: MinecraftClient) {
        val player = client.player ?: return
        
        println("[SFX] Executing: Jump")
        
        if (player.isOnGround) {
            KeyBinding.setKeyPressed(client.options.jumpKey.defaultKey, true)
            client.execute {
                kotlin.runCatching {
                    Thread.sleep(100)
                    KeyBinding.setKeyPressed(client.options.jumpKey.defaultKey, false)
                }
            }
        }
    }
    
    /**
     * 转头随机角度再转回来 - 平滑旋转过渡
     */
    fun rotateHead(client: MinecraftClient) {
        val player = client.player ?: return
        
        when (rotationPhase) {
            RotationPhase.IDLE -> {
                val randomAngle = Random.nextDouble(-60.0, 60.0).toFloat()
                originalYaw = player.yaw
                targetYaw = originalYaw + randomAngle
                rotationPhase = RotationPhase.ROTATING_AWAY
                rotationTimer = ROTATION_DURATION
                
                println("[SFX] Executing: Rotate head by $randomAngle degrees")
            }
            
            RotationPhase.ROTATING_AWAY -> {
                rotationTimer--
                val progress = 1.0f - (rotationTimer.toFloat() / ROTATION_DURATION.toFloat())
                player.yaw = lerpAngle(originalYaw, targetYaw, progress)
                
                if (rotationTimer <= 0) {
                    rotationPhase = RotationPhase.HOLDING
                    rotationTimer = 20 // 保持1秒
                    println("[SFX] Rotation held")
                }
            }
            
            RotationPhase.HOLDING -> {
                rotationTimer--
                if (rotationTimer <= 0) {
                    rotationPhase = RotationPhase.ROTATING_BACK
                    rotationTimer = ROTATION_DURATION
                    println("[SFX] Rotating back to original position")
                }
            }
            
            RotationPhase.ROTATING_BACK -> {
                rotationTimer--
                val progress = 1.0f - (rotationTimer.toFloat() / ROTATION_DURATION.toFloat())
                player.yaw = lerpAngle(targetYaw, originalYaw, progress)
                
                if (rotationTimer <= 0) {
                    player.yaw = originalYaw
                    rotationPhase = RotationPhase.IDLE
                    println("[SFX] Rotation completed, restored to original yaw")
                }
            }
        }
    }
    
    /**
     * 线性插值计算角度（处理角度跨越360度的情况）
     */
    private fun lerpAngle(start: Float, end: Float, progress: Float): Float {
        var delta = end - start
        while (delta > 180.0f) delta -= 360.0f
        while (delta < -180.0f) delta += 360.0f
        return start + delta * progress
    }

    /**
     * 检查是否正在执行转头动作
     */
    fun isExecutingRotation(): Boolean {
        return rotationPhase != RotationPhase.IDLE
    }
}
