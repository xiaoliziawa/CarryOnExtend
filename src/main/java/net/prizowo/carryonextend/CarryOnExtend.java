package net.prizowo.carryonextend;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.prizowo.carryonextend.registry.EntityRegistry;
import net.prizowo.carryonextend.trigger.TriggerRegistry;
import org.slf4j.Logger;

@Mod(CarryOnExtend.MOD_ID)
public class CarryOnExtend {
    public static final String MOD_ID = "carryonextend";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CarryOnExtend(IEventBus modEventBus) {
        EntityRegistry.ENTITIES.register(modEventBus);
        TriggerRegistry.register(modEventBus);
    }
}