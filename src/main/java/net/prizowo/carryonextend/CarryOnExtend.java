package net.prizowo.carryonextend;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.prizowo.carryonextend.registry.EntityRegistry;
import org.slf4j.Logger;

@Mod(CarryOnExtend.MOD_ID)
public class CarryOnExtend {
    public static final String MOD_ID = "carryonextend";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CarryOnExtend(IEventBus modEventBus) {
        EntityRegistry.ENTITIES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CarryOnExtend mod is initializing...");
    }
}