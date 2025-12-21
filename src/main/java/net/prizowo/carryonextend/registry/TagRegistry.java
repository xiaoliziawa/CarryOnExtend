package net.prizowo.carryonextend.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.prizowo.carryonextend.CarryOnExtend;

public class TagRegistry {
    public static class Blocks {
        public static final TagKey<Block> ALWAYS_DROP = tag("always_drop");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(CarryOnExtend.MOD_ID, name));
        }
    }
}
