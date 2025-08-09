package net.prizowo.carryonextend.trigger;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.prizowo.carryonextend.CarryOnExtend;

public class TntThrowTrigger extends SimpleCriterionTrigger<TntThrowTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(CarryOnExtend.MOD_ID, "tnt_throw");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (triggerInstance) -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate player) {
            super(TntThrowTrigger.ID, player);
        }

        public static TriggerInstance tntThrow() {
            return new TriggerInstance(ContextAwarePredicate.ANY);
        }
    }
}