package net.prizowo.carryonextend.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.prizowo.carryonextend.CarryOnExtend;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CustomFallingBlockEntity extends FallingBlockEntity {
    private static final EntityDataAccessor<CompoundTag> BLOCK_DATA = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    
    private BlockState blockState;

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(EntityType.FALLING_BLOCK, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

        entity.setPos(x, y, z);
        entity.blockState = state;
        
        if (blockData != null) {
            entity.setBlockData(blockData);
        }
        
        entity.setDeltaMovement(motion);
        entity.time = 1;
        entity.dropItem = true;

        level.addFreshEntity(entity);

        return entity;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BLOCK_DATA, new CompoundTag());
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
        BlockState.CODEC.encodeStart(NbtOps.INSTANCE, this.blockState)
                .resultOrPartial(CarryOnExtend.LOGGER::error)
                .ifPresent(nbt -> tag.put("BlockState", nbt));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CustomBlockData")) {
            this.setBlockData(tag.getCompound("CustomBlockData"));
        }
        if (tag.contains("BlockState")) {
            BlockState.CODEC.parse(NbtOps.INSTANCE, tag.get("BlockState"))
                    .resultOrPartial(CarryOnExtend.LOGGER::error)
                    .ifPresent(state -> this.blockState = state);
        }
    }

    @Override
    public @NotNull BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public void tick() {
        if (this.blockState == null) {
            this.discard();
            return;
        }
        
        updateMovement();
        
        if (!this.level().isClientSide && !this.isRemoved()) {
            BlockPos pos = this.blockPosition();
            boolean canPlace = this.level().getBlockState(pos).canBeReplaced();
            
            if (this.onGround()) {
                if (canPlace) {
                    placeBlock(pos);
                } else {
                    dropAsItem();
                }
            }
        }
    }
    
    private void updateMovement() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }
    
    private void placeBlock(BlockPos pos) {
        CompoundTag blockData = this.getBlockData();
        
        if (this.level().setBlock(pos, this.blockState, 3)) {
            if (hasValidBlockEntity(pos, blockData)) {
                BlockEntity blockEntity = this.level().getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.load(blockData);
                    this.level().sendBlockUpdated(pos, this.blockState, this.blockState, 3);
                    blockEntity.setChanged();
                }
            }
            this.discard();
        }
    }
    
    private boolean hasValidBlockEntity(BlockPos pos, CompoundTag blockData) {
        return this.level().getBlockEntity(pos) != null && blockData != null && !blockData.isEmpty();
    }
    
    private void dropAsItem() {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = createItemStackWithData();
            this.spawnAtLocation(itemStack);
        }
        
        this.discard();
    }
    
    private ItemStack createItemStackWithData() {
        ItemStack itemStack = new ItemStack(this.blockState.getBlock());
        CompoundTag blockData = this.getBlockData();
        
        if (blockData == null || blockData.isEmpty() || 
            !(this.blockState.getBlock() instanceof EntityBlock entityBlock)) {
            return itemStack;
        }
        
        BlockPos pos = this.blockPosition();
        BlockEntity tempEntity = entityBlock.newBlockEntity(pos, this.blockState);
        
        if (tempEntity != null) {
            return createItemWithBlockEntityData(itemStack, tempEntity, blockData);
        }
        
        return itemStack;
    }
    
    private ItemStack createItemWithBlockEntityData(ItemStack itemStack, BlockEntity tempEntity, CompoundTag blockData) {
        tempEntity.load(blockData);
        CompoundTag fullEntityTag = tempEntity.saveWithFullMetadata();
        CompoundTag itemTag = createItemTag(fullEntityTag);
        
        itemStack.setTag(itemTag);
        return itemStack;
    }
    
    private CompoundTag createItemTag(CompoundTag fullEntityTag) {
        CompoundTag itemTag = new CompoundTag();
        
        if (fullEntityTag.contains("ForgeCaps")) {
            itemTag.put("ForgeCaps", fullEntityTag.getCompound("ForgeCaps"));
        }
        
        CompoundTag blockEntityTag = createBlockEntityTag(fullEntityTag);
        itemTag.put("BlockEntityTag", blockEntityTag);
        
        return itemTag;
    }
    
    private CompoundTag createBlockEntityTag(CompoundTag fullEntityTag) {
        CompoundTag blockEntityTag = new CompoundTag();
        
        for (String key : fullEntityTag.getAllKeys()) {
            if (!key.equals("x") && !key.equals("y") && !key.equals("z")) {
                blockEntityTag.put(key, fullEntityTag.get(key));
            }
        }
        
        return blockEntityTag;
    }
    
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return super.getCapability(capability, facing);
    }
}