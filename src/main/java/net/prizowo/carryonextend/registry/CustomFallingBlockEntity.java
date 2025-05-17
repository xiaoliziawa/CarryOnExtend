package net.prizowo.carryonextend.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.prizowo.carryonextend.util.FallingBlockEntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomFallingBlockEntity extends FallingBlockEntity {
    private static final EntityDataAccessor<CompoundTag> BLOCK_DATA = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private boolean shouldDropItems = false;
    private boolean handledDrops = false;
    private BlockState cachedBlockState = null;

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        return FallingBlockEntityUtil.throwBlock(level, x, y, z, state, blockData, motion);
    }

    public void setCachedBlockState(BlockState state) {
        this.cachedBlockState = state;
    }

    public void setDropItem(boolean dropItem) {
        this.dropItem = dropItem;
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
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("CustomBlockData", this.getBlockData());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CustomBlockData")) {
            this.setBlockData(tag.getCompound("CustomBlockData"));
        }
    }

    @Override
    public void tick() {
        CompoundTag customData = this.getBlockData();
        
        BlockState blockState = FallingBlockEntityUtil.getBlockState(this);
        if (blockState == null && cachedBlockState != null) {
            blockState = cachedBlockState;
            FallingBlockEntityUtil.setBlockState(this, cachedBlockState);
        }
        
        if (blockState == null) {
            this.discard();
            return;
        }
        
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        
        this.move(MoverType.SELF, this.getDeltaMovement());
        
        BlockPos pos = this.blockPosition();
        
        if (!this.level().isClientSide) {
            if (this.isRemoved()) {
                return;
            }
            
            boolean canPlace = this.level().getBlockState(pos).canBeReplaced();
            boolean onGround = this.onGround();
            
            if (onGround && canPlace) {
                if (FallingBlockEntityUtil.placeBlock(this, pos, blockState, customData, this.level())) {
                    return;
                }
            } else if (onGround && !handledDrops) {
                if (this.level().getServer().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    ItemStack itemStack = FallingBlockEntityUtil.createItemWithBlockEntityData(
                            blockState, customData, pos, this.level());
                    
                    if (this.level() instanceof ServerLevel serverLevel) {
                        this.spawnAtLocationCustom(serverLevel, itemStack);
                    }
                    handledDrops = true;
                }
                
                this.discard();
            }
        }
        
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }
    
    @Nullable
    public ItemEntity spawnAtLocationCustom(ServerLevel level, ItemStack stack) {
        return this.spawnAtLocationCustom(level, stack, 0.0F);
    }

    @Nullable
    public ItemEntity spawnAtLocationCustom(ServerLevel level, ItemStack stack, float yOffset) {
        if (stack.isEmpty()) {
            return null;
        } else {
            ItemEntity itementity = new ItemEntity(level, this.getX(), this.getY() + (double)yOffset, this.getZ(), stack);
            itementity.setDefaultPickUpDelay();
            if (this.captureDrops() != null) {
                this.captureDrops().add(itementity);
            } else {
                level.addFreshEntity(itementity);
            }

            return itementity;
        }
    }
}