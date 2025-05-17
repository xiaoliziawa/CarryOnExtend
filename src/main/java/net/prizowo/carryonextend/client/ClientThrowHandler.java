package net.prizowo.carryonextend.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.prizowo.carryonextend.CarryOnExtend;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, value = Dist.CLIENT)
public class ClientThrowHandler {
    
    private static final int THROW_EFFECT_DURATION = 10; // 半秒，假设20 ticks/秒
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        Player player = mc.player;
        
        if (player.getPersistentData().contains("ThrowTime")) {
            long throwTime = player.getPersistentData().getLong("ThrowTime");
            long currentTime = player.level().getGameTime();
            long elapsedTicks = currentTime - throwTime;
            
            if (elapsedTicks < THROW_EFFECT_DURATION) {
                double vx = player.getPersistentData().getDouble("ThrowVelocityX");
                double vy = player.getPersistentData().getDouble("ThrowVelocityY");
                double vz = player.getPersistentData().getDouble("ThrowVelocityZ");
                
                Vec3 currentMotion = player.getDeltaMovement();
                
                float factor = 1.0f - (elapsedTicks / (float)THROW_EFFECT_DURATION);
                
                double additionalY = currentMotion.y < 0 ? 0.08 * factor : 0;
                
                double newVx = currentMotion.x * 0.6 + vx * 0.4 * factor;
                double newVy = Math.max(currentMotion.y, vy * factor) + additionalY;
                double newVz = currentMotion.z * 0.6 + vz * 0.4 * factor;
                
                player.setDeltaMovement(newVx, newVy, newVz);
                player.hurtMarked = true;
                
                player.setOnGround(false);
                player.fallDistance = 0.0f;
            } else {
                player.getPersistentData().remove("ThrowTime");
                player.getPersistentData().remove("ThrowVelocityX");
                player.getPersistentData().remove("ThrowVelocityY");
                player.getPersistentData().remove("ThrowVelocityZ");
            }
        }
    }
} 