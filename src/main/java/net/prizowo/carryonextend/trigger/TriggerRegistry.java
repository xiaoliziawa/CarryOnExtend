package net.prizowo.carryonextend.trigger;

import net.minecraft.advancements.CriteriaTriggers;

public class TriggerRegistry {
    
    public static final TntThrowTrigger TNT_THROW = new TntThrowTrigger();
    public static final SelfDestructionTrigger SELF_DESTRUCTION = new SelfDestructionTrigger();

    public static void register() {
        CriteriaTriggers.register(TNT_THROW);
        CriteriaTriggers.register(SELF_DESTRUCTION);
    }
}