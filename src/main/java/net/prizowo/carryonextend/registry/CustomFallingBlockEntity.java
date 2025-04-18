package net.prizowo.carryonextend.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.prizowo.carryonextend.CarryOnExtend;

import java.lang.reflect.Field;

public class CustomFallingBlockEntity extends FallingBlockEntity {
    private static final EntityDataAccessor<CompoundTag> BLOCK_DATA = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static Field blockStateField;

    static {
        try {
            blockStateField = FallingBlockEntity.class.getDeclaredField("blockState");
            blockStateField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            CarryOnExtend.LOGGER.error("无法获取FallingBlockEntity的blockState字段", e);
        }
    }

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                net.prizowo.carryonextend.registry.EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

        entity.setPos(x, y, z);

        try {
            if (blockStateField != null) {
                blockStateField.set(entity, state);
            }
        } catch (IllegalAccessException e) {
            CarryOnExtend.LOGGER.error("设置FallingBlockEntity的blockState失败", e);
            return null;
        }

        entity.setBlockData(blockData);

        entity.setDeltaMovement(motion);

        entity.time = 1;
        entity.dropItem = true;

        level.addFreshEntity(entity);

        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLOCK_DATA, new CompoundTag());
    }

    public void setBlockData(CompoundTag blockData) {
        this.entityData.set(BLOCK_DATA, blockData);
    }

    public CompoundTag getBlockData() {
        return this.entityData.get(BLOCK_DATA);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("CustomBlockData", this.getBlockData());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CustomBlockData")) {
            this.setBlockData(tag.getCompound("CustomBlockData"));
        }
    }

    @Override
    public void tick() {
        CompoundTag customData = this.getBlockData();
        
        boolean aboutToLand = this.getDeltaMovement().y < 0 && this.getDeltaMovement().lengthSqr() < 0.02;
        
        super.tick();
        
        if (aboutToLand && this.isRemoved()) {
            BlockState blockState = level().getBlockState(blockPosition());
            
            if (level().getBlockEntity(blockPosition()) != null && customData != null && !customData.isEmpty()) {
                level().getBlockEntity(blockPosition()).loadWithComponents(customData, level().registryAccess());
                
                level().sendBlockUpdated(blockPosition(), blockState, blockState, 3);
                
                level().getBlockEntity(blockPosition()).setChanged();
            }
        }
        
        if (this.blockData == null && customData != null && !customData.isEmpty()) {
            this.blockData = customData;
        }
    }
} 