package net.prizowo.carryonextend.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.client.renderer.CustomFallingBlockRenderer;
import net.prizowo.carryonextend.registry.EntityRegistry;

@Mod.EventBusSubscriber(modid = CarryOnExtend.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.CUSTOM_FALLING_BLOCK.get(), CustomFallingBlockRenderer::new);
    }
}