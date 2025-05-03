package net.prizowo.carryonextend.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import net.prizowo.carryonextend.registry.EntityRegistry;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.Objects;

public class FallingBlockUtil {
    
    /**
     * 检查实体NBT数据是否是自定义掉落方块
     * 
     * @param entityNBT 实体NBT数据
     * @return 是否是自定义掉落方块
     */
    public static boolean isCustomFallingBlock(CompoundTag entityNBT) {
        if (entityNBT == null || !entityNBT.contains("id")) {
            return false;
        }
        
        String entityId = entityNBT.getString("id");
        ResourceLocation entityTypeId = new ResourceLocation(entityId);
        
        EntityType<?> customFallingBlockType = EntityRegistry.CUSTOM_FALLING_BLOCK.get();
        ResourceLocation customTypeId = ForgeRegistries.ENTITY_TYPES.getKey(customFallingBlockType);
        
        return customTypeId != null && customTypeId.equals(entityTypeId);
    }
    
    /**
     * 从NBT数据中获取方块状态
     * 
     * @param entityNBT 实体NBT数据
     * @return 方块状态，如果无法获取则返回石头
     */
    public static BlockState getBlockStateFromNBT(CompoundTag entityNBT) {
        if (entityNBT == null) {
            return Blocks.STONE.defaultBlockState();
        }
        
        BlockState blockState = null;
        
        if (entityNBT.contains("BlockState")) {
            try {
                CompoundTag blockStateTag = entityNBT.getCompound("BlockState");
                int blockStateId = blockStateTag.getInt("id");
                if (blockStateId > 0) {
                    blockState = Block.stateById(blockStateId);
                    if (blockState != null && blockState.getBlock() != Blocks.AIR) {
                        return blockState;
                    }
                }
            } catch (Exception e) {
            }
        }
        
        if (entityNBT.contains("BlockId")) {
            try {
                String blockIdStr = entityNBT.getString("BlockId");
                ResourceLocation blockId = new ResourceLocation(blockIdStr);
                Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                if (block != null && block != Blocks.AIR) {
                    return block.defaultBlockState();
                }
            } catch (Exception e) {
            }
        }
        
        CompoundTag blockData = null;
        if (entityNBT.contains("CustomBlockData")) {
            blockData = entityNBT.getCompound("CustomBlockData");
        }
        
        if (entityNBT.contains("EntityData")) {
            try {
                CompoundTag entityData = entityNBT.getCompound("EntityData");
                if (entityData.contains("BLOCK_ID")) {
                    String blockId = entityData.getString("BLOCK_ID");
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    if (block != null && block != Blocks.AIR) {
                        blockState = block.defaultBlockState();
                    }
                }
                
                if (blockState == null && entityData.contains("BLOCK_STATE_META")) {
                    int stateId = entityData.getInt("BLOCK_STATE_META");
                    if (stateId != 0) {
                        blockState = Block.stateById(stateId);
                        if (blockState != null && blockState.getBlock() != Blocks.AIR) {
                            return blockState;
                        }
                    }
                }
                
                if (blockState != null) {
                    return blockState;
                }
            } catch (Exception e) {
            }
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * 从NBT数据中获取方块数据
     * 
     * @param entityNBT 实体NBT数据
     * @return 方块数据，可能为null
     */
    public static CompoundTag getBlockData(CompoundTag entityNBT) {
        if (entityNBT == null) {
            return null;
        }
        
        if (entityNBT.contains("CustomBlockData")) {
            return entityNBT.getCompound("CustomBlockData");
        }
        
        return null;
    }
    
    /**
     * 保存方块状态的所有数据到NBT标签
     * 
     * @param tag NBT标签
     * @param blockState 方块状态
     * @param blockData 方块数据
     * @param blockIdValue 方块ID字符串
     * @param blockStateMetaValue 方块状态元数据
     */
    public static void saveBlockDataToNBT(CompoundTag tag, BlockState blockState, 
                                          CompoundTag blockData, String blockIdValue, 
                                          int blockStateMetaValue) {
        if (blockData != null) {
            tag.put("CustomBlockData", blockData);
        }
        
        tag.putString("CustomEntityType", "falling_block");
        
        if (blockState != null) {
            int stateId = Block.getId(blockState);
            CompoundTag blockStateTag = new CompoundTag();
            blockStateTag.putInt("id", stateId);
            tag.put("BlockState", blockStateTag);
            
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
            if (blockId != null) {
                tag.putString("BlockId", blockId.toString());
            }
        }
        
        CompoundTag entityData = new CompoundTag();
        entityData.putString("BLOCK_ID", blockIdValue);
        entityData.putInt("BLOCK_STATE_META", blockStateMetaValue);
        tag.put("EntityData", entityData);
    }
    
    /**
     * 处理掉落方块的投掷
     * 
     * @param player 玩家
     * @param carry 携带数据
     * @param entityNBT 实体NBT数据 
     * @param throwPower 投掷力度
     * @param throwUpward 垂直投掷力度
     * @param powerFactor 力度因子
     * @return 是否成功处理
     */
    public static boolean handleFallingBlockThrow(ServerPlayer player, CarryOnData carry, 
                                                 CompoundTag entityNBT, float throwPower, 
                                                 float throwUpward, float powerFactor) {
        Level level = player.level();
        
        if (!isCustomFallingBlock(entityNBT)) {
            return false;
        }
        
        BlockState blockState = getBlockStateFromNBT(entityNBT);
        CompoundTag blockData = getBlockData(entityNBT);
        
        if (blockState == null || blockState.getBlock() == Blocks.AIR) {
            return false;
        }
        
        Vec3 playerPos = player.position().add(0, 1.5, 0);
        Vec3 lookDir = player.getLookAngle();
        Vec3 motion = new Vec3(lookDir.x * throwPower, throwUpward, lookDir.z * throwPower);
        
        CustomFallingBlockEntity fallingBlock = CustomFallingBlockEntity.throwBlock(
                level, playerPos.x, playerPos.y, playerPos.z, blockState, blockData, motion);
        
        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
        
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        
        return true;
    }
    
    /**
     * 检查是否有有效的方块实体
     * 
     * @param level 世界
     * @param pos 位置
     * @param blockData 方块数据
     * @return 是否有有效的方块实体
     */
    public static boolean hasValidBlockEntity(Level level, BlockPos pos, CompoundTag blockData) {
        return level.getBlockEntity(pos) != null && blockData != null && !blockData.isEmpty();
    }
    
    /**
     * 放置方块到世界中
     * 
     * @param level 世界
     * @param pos 位置
     * @param blockState 方块状态
     * @param blockData 方块数据
     * @return 是否成功放置
     */
    public static boolean placeBlock(Level level, BlockPos pos, BlockState blockState, CompoundTag blockData) {
        if (level.setBlock(pos, blockState, 3)) {
            if (hasValidBlockEntity(level, pos, blockData)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.load(blockData);
                    level.sendBlockUpdated(pos, blockState, blockState, 3);
                    blockEntity.setChanged();
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * 创建带有方块实体数据的物品堆叠
     * 
     * @param blockState 方块状态
     * @param blockData 方块数据
     * @param pos 位置
     * @return 物品堆叠
     */
    public static ItemStack createItemStackWithData(BlockState blockState, CompoundTag blockData, BlockPos pos) {
        ItemStack itemStack = new ItemStack(blockState.getBlock());
        
        if (blockData == null || blockData.isEmpty() || 
            !(blockState.getBlock() instanceof EntityBlock entityBlock)) {
            return itemStack;
        }
        
        BlockEntity tempEntity = entityBlock.newBlockEntity(pos, blockState);
        
        if (tempEntity != null) {
            tempEntity.load(blockData);
            CompoundTag fullEntityTag = tempEntity.saveWithFullMetadata();
            CompoundTag itemTag = new CompoundTag();
            
            if (fullEntityTag.contains("ForgeCaps")) {
                itemTag.put("ForgeCaps", fullEntityTag.getCompound("ForgeCaps"));
            }
            
            CompoundTag blockEntityTag = new CompoundTag();
            
            for (String key : fullEntityTag.getAllKeys()) {
                if (!key.equals("x") && !key.equals("y") && !key.equals("z")) {
                    blockEntityTag.put(key, Objects.requireNonNull(fullEntityTag.get(key)));
                }
            }
            
            itemTag.put("BlockEntityTag", blockEntityTag);
            itemStack.setTag(itemTag);
        }
        
        return itemStack;
    }
} 