package net.prizowo.carryonextend.datagen;

import net.minecraft.advancements.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.trigger.TntThrowTrigger;
import net.prizowo.carryonextend.trigger.SelfDestructionTrigger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new ModAdvancements()));
    }

    public static class ModAdvancements implements AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer, ExistingFileHelper existingFileHelper) {
            AdvancementHolder tntThrowerAdvancement = Advancement.Builder.advancement()
                    .display(Items.TNT,
                            Component.translatable("advancement.carryonextend.tnt_thrower.title"),
                            Component.translatable("advancement.carryonextend.tnt_thrower.description"),
                            ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                            AdvancementType.TASK,
                            true, true, false)
                    .addCriterion("throw_tnt", TntThrowTrigger.TriggerInstance.tntThrow())
                    .save(writer, CarryOnExtend.MOD_ID + ":tnt_thrower");

            AdvancementHolder selfDestructionAdvancement = Advancement.Builder.advancement()
                    .parent(tntThrowerAdvancement)
                    .display(Items.TNT_MINECART,
                            Component.translatable("advancement.carryonextend.self_destruction.title"),
                            Component.translatable("advancement.carryonextend.self_destruction.description"),
                            null,
                            AdvancementType.GOAL,
                            true, true, true)
                    .addCriterion("self_destruction", SelfDestructionTrigger.TriggerInstance.selfDestruction())
                    .save(writer, CarryOnExtend.MOD_ID + ":self_destruction");
        }
    }
}