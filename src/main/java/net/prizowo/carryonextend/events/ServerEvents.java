package net.prizowo.carryonextend.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.trigger.TriggerRegistry;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        DamageSource damageSource = event.getSource();
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof PrimedTnt tnt) {
            if (tnt.getTags().contains("thrownBy:" + player.getUUID().toString())) {
                TriggerRegistry.SELF_DESTRUCTION.get().trigger(player);
            }
        }
    }
}