package net.prizowo.carryonextend;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.prizowo.carryonextend.registry.EntityRegistry;
import net.prizowo.carryonextend.trigger.TriggerRegistry;
import org.slf4j.Logger;

@Mod(CarryOnExtend.MOD_ID)
public class CarryOnExtend {
    public static final String MOD_ID = "carryonextend";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CarryOnExtend() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EntityRegistry.ENTITIES.register(modEventBus);
        
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(TriggerRegistry::register);
    }
}