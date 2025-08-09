package net.prizowo.carryonextend.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.trigger.TntThrowTrigger;
import net.prizowo.carryonextend.trigger.SelfDestructionTrigger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends ForgeAdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new ModAdvancements()));
    }

    public static class ModAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper existingFileHelper) {
            Advancement tntThrowerAdvancement = Advancement.Builder.advancement()
                    .display(new DisplayInfo(
                            new ItemStack(Items.TNT),
                            Component.translatable("advancement.carryonextend.tnt_thrower.title"),
                            Component.translatable("advancement.carryonextend.tnt_thrower.description"),
                            new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.TASK,
                            true, true, false))
                    .addCriterion("throw_tnt", TntThrowTrigger.TriggerInstance.tntThrow())
                    .save(saver, new ResourceLocation(CarryOnExtend.MOD_ID, "tnt_thrower"), existingFileHelper);

            Advancement.Builder.advancement()
                    .parent(tntThrowerAdvancement)
                    .display(new DisplayInfo(
                            new ItemStack(Items.TNT_MINECART),
                            Component.translatable("advancement.carryonextend.self_destruction.title"),
                            Component.translatable("advancement.carryonextend.self_destruction.description"),
                            null,
                            FrameType.GOAL,
                            true, true, true))
                    .addCriterion("self_destruction", SelfDestructionTrigger.TriggerInstance.selfDestruction())
                    .save(saver, new ResourceLocation(CarryOnExtend.MOD_ID, "self_destruction"), existingFileHelper);
        }
    }
}