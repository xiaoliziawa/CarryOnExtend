package net.prizowo.carryonextend.client;

import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.registry.EntityRegistry;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.CUSTOM_FALLING_BLOCK.get(),
                FallingBlockRenderer::new);
    }
} 