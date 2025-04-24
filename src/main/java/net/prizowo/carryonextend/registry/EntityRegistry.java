package net.prizowo.carryonextend.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.prizowo.carryonextend.CarryOnExtend;

import java.util.function.Supplier;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CarryOnExtend.MOD_ID);

    public static final Supplier<EntityType<CustomFallingBlockEntity>> CUSTOM_FALLING_BLOCK = ENTITIES.register(
        "custom_falling_block",
        () -> EntityType.Builder.<CustomFallingBlockEntity>of(CustomFallingBlockEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(10)
            .updateInterval(20)
            .build(CarryOnExtend.MOD_ID + ":custom_falling_block")
    );
} 