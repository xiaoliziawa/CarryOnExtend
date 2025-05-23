package net.prizowo.carryonextend.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.client.renderer.CustomFallingBlockRenderer;
import net.prizowo.carryonextend.registry.EntityRegistry;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(EntityRegistry.CUSTOM_FALLING_BLOCK.get(), CustomFallingBlockRenderer::new);
        });
    }
} 