package net.prizowo.carryonextend.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.trigger.TriggerRegistry;

@Mod.EventBusSubscriber(modid = CarryOnExtend.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DamageSource damageSource = event.getSource();
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof PrimedTnt tnt) {
            if (tnt.getTags().contains("thrownBy:" + player.getUUID())) {
                TriggerRegistry.SELF_DESTRUCTION.trigger(player);
            }
        }
    }
}