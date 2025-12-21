package net.prizowo.carryonextend.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.registry.TagRegistry;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CarryOnExtend.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(TagRegistry.Blocks.ALWAYS_DROP)
                // Glass
                .add(Blocks.GLASS)
                .add(Blocks.WHITE_STAINED_GLASS)
                .add(Blocks.ORANGE_STAINED_GLASS)
                .add(Blocks.MAGENTA_STAINED_GLASS)
                .add(Blocks.LIGHT_BLUE_STAINED_GLASS)
                .add(Blocks.YELLOW_STAINED_GLASS)
                .add(Blocks.LIME_STAINED_GLASS)
                .add(Blocks.PINK_STAINED_GLASS)
                .add(Blocks.GRAY_STAINED_GLASS)
                .add(Blocks.LIGHT_GRAY_STAINED_GLASS)
                .add(Blocks.CYAN_STAINED_GLASS)
                .add(Blocks.PURPLE_STAINED_GLASS)
                .add(Blocks.BLUE_STAINED_GLASS)
                .add(Blocks.BROWN_STAINED_GLASS)
                .add(Blocks.GREEN_STAINED_GLASS)
                .add(Blocks.RED_STAINED_GLASS)
                .add(Blocks.BLACK_STAINED_GLASS)
                .add(Blocks.TINTED_GLASS)

                // Glass panes
                .add(Blocks.GLASS_PANE)
                .add(Blocks.WHITE_STAINED_GLASS_PANE)
                .add(Blocks.ORANGE_STAINED_GLASS_PANE)
                .add(Blocks.MAGENTA_STAINED_GLASS_PANE)
                .add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
                .add(Blocks.YELLOW_STAINED_GLASS_PANE)
                .add(Blocks.LIME_STAINED_GLASS_PANE)
                .add(Blocks.PINK_STAINED_GLASS_PANE)
                .add(Blocks.GRAY_STAINED_GLASS_PANE)
                .add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
                .add(Blocks.CYAN_STAINED_GLASS_PANE)
                .add(Blocks.PURPLE_STAINED_GLASS_PANE)
                .add(Blocks.BLUE_STAINED_GLASS_PANE)
                .add(Blocks.BROWN_STAINED_GLASS_PANE)
                .add(Blocks.GREEN_STAINED_GLASS_PANE)
                .add(Blocks.RED_STAINED_GLASS_PANE)
                .add(Blocks.BLACK_STAINED_GLASS_PANE)

                // Watermelon and pumpkins
                .add(Blocks.MELON)
                .add(Blocks.PUMPKIN)
                .add(Blocks.CARVED_PUMPKIN)
                .add(Blocks.JACK_O_LANTERN)

                // Fragile light blocks
                .add(Blocks.GLOWSTONE)
                .add(Blocks.SEA_LANTERN)

                // Ice
                .add(Blocks.ICE)
                .add(Blocks.PACKED_ICE)
                .add(Blocks.BLUE_ICE);
    }
}
