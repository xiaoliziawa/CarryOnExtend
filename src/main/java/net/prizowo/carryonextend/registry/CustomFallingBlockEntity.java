package net.prizowo.carryonextend.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.prizowo.carryonextend.CarryOnExtend;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class CustomFallingBlockEntity extends FallingBlockEntity {
    private static final EntityDataAccessor<CompoundTag> BLOCK_DATA = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static Field blockStateField;
    private static Field cancelDropField;
    private static Field timeField;
    private boolean shouldDropItems = false;
    private boolean handledDrops = false;
    private BlockState cachedBlockState = null;

    static {
        try {
            blockStateField = FallingBlockEntity.class.getDeclaredField("blockState");
            blockStateField.setAccessible(true);
            
            try {
                cancelDropField = FallingBlockEntity.class.getDeclaredField("cancelDrop");
                cancelDropField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                CarryOnExtend.LOGGER.warn("Unable to obtain the cancelDrop field of FallingBlockEntity", e);
            }
            
            try {
                timeField = FallingBlockEntity.class.getDeclaredField("time");
                timeField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                CarryOnExtend.LOGGER.warn("Unable to retrieve the time field of FallingBlockEntity", e);
            }
            
        } catch (NoSuchFieldException e) {
            CarryOnExtend.LOGGER.error("Unable to retrieve the blockState field of FallingBlockEntity", e);
        }
    }

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                net.prizowo.carryonextend.registry.EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

        entity.setPos(x, y, z);
        entity.cachedBlockState = state;

        try {
            if (blockStateField != null) {
                blockStateField.set(entity, state);
            }
            
        } catch (IllegalAccessException e) {
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
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
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
        
        BlockState blockState = null;
        try {
            if (blockStateField != null) {
                blockState = (BlockState) blockStateField.get(this);
                if (blockState == null && cachedBlockState != null) {
                    blockState = cachedBlockState;
                    blockStateField.set(this, cachedBlockState);
                }
            }
        } catch (IllegalAccessException e) {
            CarryOnExtend.LOGGER.error("Failed to obtain the blockState of FallingBlockEntity", e);
        }
        
        BlockPos currentPos = this.blockPosition();
        boolean wasOnGround = this.onGround();
        boolean wasRemoved = this.isRemoved();
        
        super.tick();
        
        boolean isRemovedNow = this.isRemoved();
        boolean isOnGroundNow = this.onGround();
        
        if (!wasOnGround && isOnGroundNow && blockState != null) {
            shouldDropItems = true;
        }
        
        if (!wasRemoved && isRemovedNow && blockState != null) {
            Block block = blockState.getBlock();
            BlockPos finalPos = this.blockPosition();
            

            if (level().getBlockState(finalPos).is(block)) {
                if (level().getBlockEntity(finalPos) != null && customData != null && !customData.isEmpty()) {
                    level().getBlockEntity(finalPos).loadWithComponents(customData, level().registryAccess());
                    level().sendBlockUpdated(finalPos, level().getBlockState(finalPos), level().getBlockState(finalPos), 3);
                    level().getBlockEntity(finalPos).setChanged();
                }
            }
            else if (customData != null && !customData.isEmpty() && blockState.hasBlockEntity() &&
                    !handledDrops && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                NonNullList<ItemStack> items = NonNullListFromNBT(customData);
                if (!items.isEmpty()) {
                    Containers.dropContents(level(), finalPos, items);
                    handledDrops = true;
                }
            }
        }
        
        if (this.blockData == null && customData != null && !customData.isEmpty()) {
            this.blockData = customData;
        }
    }
    
    private NonNullList<ItemStack> NonNullListFromNBT(CompoundTag data) {
        NonNullList<ItemStack> items = NonNullList.create();
        
        if (data != null && data.contains("Items", 9)) {
            net.minecraft.nbt.ListTag itemsList = data.getList("Items", 10);
            
            for (int i = 0; i < itemsList.size(); i++) {
                CompoundTag itemTag = itemsList.getCompound(i);
                ItemStack stack = ItemStack.parseOptional(level().registryAccess(), itemTag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        
        return items;
    }
}