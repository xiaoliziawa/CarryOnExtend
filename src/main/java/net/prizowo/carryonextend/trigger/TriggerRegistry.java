package net.prizowo.carryonextend.trigger;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.carryonextend.CarryOnExtend;

import java.util.function.Supplier;

public class TriggerRegistry {
    
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, CarryOnExtend.MOD_ID);

    public static final Supplier<TntThrowTrigger> TNT_THROW = TRIGGERS.register("tnt_throw", TntThrowTrigger::new);
    public static final Supplier<SelfDestructionTrigger> SELF_DESTRUCTION = TRIGGERS.register("self_destruction", SelfDestructionTrigger::new);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}