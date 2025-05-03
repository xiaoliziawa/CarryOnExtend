package net.prizowo.carryonextend.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.prizowo.carryonextend.util.FallingBlockUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CustomFallingBlockEntity extends FallingBlockEntity {
    private static final EntityDataAccessor<CompoundTag> BLOCK_DATA = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<String> BLOCK_ID = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BLOCK_STATE_META = SynchedEntityData.defineId(CustomFallingBlockEntity.class, EntityDataSerializers.INT);
    
    private BlockState blockState;
    private int ticksExisted = 0;
    private boolean needsStateUpdate = false;

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        if (state == null) {
            state = Blocks.STONE.defaultBlockState();
        }

        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

        entity.setPos(x, y, z);
        entity.setBlockState(state);
        
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
        this.entityData.define(BLOCK_ID, "minecraft:stone");
        this.entityData.define(BLOCK_STATE_META, 0);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        
        if (key.equals(BLOCK_ID) || key.equals(BLOCK_STATE_META)) {
            this.needsStateUpdate = true;
        }
    }

    public void setBlockState(BlockState state) {
        if (state == null) {
            state = Blocks.STONE.defaultBlockState();
        }
        
        this.blockState = state;
        
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId != null) {
            this.entityData.set(BLOCK_ID, blockId.toString());
        }
        
        int stateId = Block.getId(state);
        this.entityData.set(BLOCK_STATE_META, stateId);
    }

    private void updateBlockStateFromData() {
        if (this.needsStateUpdate || this.blockState == null || this.blockState.getBlock() == Blocks.STONE) {
            String blockIdStr = this.entityData.get(BLOCK_ID);
            int stateId = this.entityData.get(BLOCK_STATE_META);
            
            try {
                if (!blockIdStr.isEmpty() && stateId != 0) {
                    BlockState stateFromId = Block.stateById(stateId);
                    if (stateFromId != Blocks.AIR.defaultBlockState()) {
                        this.blockState = stateFromId;
                        this.needsStateUpdate = false;
                        return;
                    }
                    
                    ResourceLocation blockId = new ResourceLocation(blockIdStr);
                    Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                    if (block != null && block != Blocks.AIR) {
                        this.blockState = block.defaultBlockState();
                        this.needsStateUpdate = false;
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (this.blockState == null) {
                this.blockState = Blocks.STONE.defaultBlockState();
            }
            this.needsStateUpdate = false;
        }
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
        
        FallingBlockUtil.saveBlockDataToNBT(
            tag, 
            this.getBlockState(),
            this.getBlockData(),
            this.entityData.get(BLOCK_ID),
            this.entityData.get(BLOCK_STATE_META)
        );
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        
        if (tag.contains("CustomBlockData")) {
            this.setBlockData(tag.getCompound("CustomBlockData"));
        }
        
        if (tag.contains("BlockState")) {
            try {
                CompoundTag blockStateTag = tag.getCompound("BlockState");
                int stateId = blockStateTag.getInt("id");
                if (stateId > 0) {
                    this.blockState = Block.stateById(stateId);
                    this.needsStateUpdate = false;
                    
                    ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(this.blockState.getBlock());
                    if (blockId != null) {
                        this.entityData.set(BLOCK_ID, blockId.toString());
                    }
                    this.entityData.set(BLOCK_STATE_META, stateId);
                }
            } catch (Exception e) {
            }
        }
        
        if ((this.blockState == null || this.blockState.getBlock() == Blocks.STONE) && tag.contains("BlockId")) {
            try {
                String blockIdStr = tag.getString("BlockId");
                ResourceLocation blockId = new ResourceLocation(blockIdStr);
                Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                if (block != null && block != Blocks.AIR) {
                    this.blockState = block.defaultBlockState();
                    this.entityData.set(BLOCK_ID, blockIdStr);
                    this.needsStateUpdate = false;
                }
            } catch (Exception e) {
            }
        }
        
        if ((this.blockState == null || this.blockState.getBlock() == Blocks.STONE) && tag.contains("EntityData")) {
            try {
                CompoundTag entityData = tag.getCompound("EntityData");
                if (entityData.contains("BLOCK_ID") && entityData.contains("BLOCK_STATE_META")) {
                    String blockIdStr = entityData.getString("BLOCK_ID");
                    int stateId = entityData.getInt("BLOCK_STATE_META");
                    
                    this.entityData.set(BLOCK_ID, blockIdStr);
                    this.entityData.set(BLOCK_STATE_META, stateId);
                    
                    if (stateId > 0) {
                        this.blockState = Block.stateById(stateId);
                    } else {
                        ResourceLocation blockId = new ResourceLocation(blockIdStr);
                        Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                        if (block != null && block != Blocks.AIR) {
                            this.blockState = block.defaultBlockState();
                        }
                    }
                    
                    this.needsStateUpdate = false;
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public @NotNull BlockState getBlockState() {
        updateBlockStateFromData();
        
        if (this.blockState == null) {
            return Blocks.STONE.defaultBlockState();
        }
        return this.blockState;
    }

    @Override
    public void tick() {
        ticksExisted++;
        
        if (ticksExisted <= 5) {
            updateBlockStateFromData();
            if (this.blockState != null && this.blockState.getBlock() == Blocks.STONE) {
                this.needsStateUpdate = true;
            }
        }
        
        if (this.blockState == null) {
            updateBlockStateFromData();
            if (this.blockState == null) {
                this.discard();
                return;
            }
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
        
        if (FallingBlockUtil.placeBlock(this.level(), pos, this.blockState, blockData)) {
            this.discard();
        }
    }
    
    private boolean hasValidBlockEntity(BlockPos pos, CompoundTag blockData) {
        return FallingBlockUtil.hasValidBlockEntity(this.level(), pos, blockData);
    }
    
    private void dropAsItem() {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = createItemStackWithData();
            this.spawnAtLocation(itemStack);
        }
        
        this.discard();
    }
    
    private ItemStack createItemStackWithData() {
        return FallingBlockUtil.createItemStackWithData(
            this.blockState,
            this.getBlockData(),
            this.blockPosition()
        );
    }
    
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return super.getCapability(capability, facing);
    }
}