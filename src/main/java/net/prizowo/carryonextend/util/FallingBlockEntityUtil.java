package net.prizowo.carryonextend.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import net.prizowo.carryonextend.registry.EntityRegistry;

import java.lang.reflect.Field;

public class FallingBlockEntityUtil {
    private static Field blockStateField;
    private static Field cancelDropField;
    private static Field timeField;

    static {
        try {
            blockStateField = FallingBlockEntity.class.getDeclaredField("blockState");
            blockStateField.setAccessible(true);

            cancelDropField = FallingBlockEntity.class.getDeclaredField("cancelDrop");
            cancelDropField.setAccessible(true);

            timeField = FallingBlockEntity.class.getDeclaredField("time");
            timeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            CarryOnExtend.LOGGER.error("Unable to retrieve the blockState field of FallingBlockEntity", e);
        }
    }

    /**
     * 创建并抛出一个自定义下落方块实体
     *
     * @param level     世界
     * @param x         x坐标
     * @param y         y坐标
     * @param z         z坐标
     * @param state     方块状态
     * @param blockData 方块数据
     * @param motion    移动向量
     * @return 创建的实体，如果创建失败则返回null
     */
    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

        entity.setPos(x, y, z);
        entity.setCachedBlockState(state);

        try {
            if (blockStateField != null) {
                blockStateField.set(entity, state);
            }
        } catch (IllegalAccessException e) {
            return null;
        }

        entity.setBlockData(blockData);
        entity.setDeltaMovement(motion);
        setEntityTime(entity, 1);
        entity.setDropItem(true);

        level.addFreshEntity(entity);

        return entity;
    }

    /**
     * 获取实体的方块状态
     *
     * @param entity 下落方块实体
     * @return 方块状态，如果获取失败则返回null
     */
    public static BlockState getBlockState(FallingBlockEntity entity) {
        try {
            if (blockStateField != null) {
                return (BlockState) blockStateField.get(entity);
            }
        } catch (IllegalAccessException e) {
            CarryOnExtend.LOGGER.error("Unable to obtain the blockState of FallingBlockEntity", e);
        }
        return null;
    }

    /**
     * 设置实体的方块状态
     *
     * @param entity 下落方块实体
     * @param state  方块状态
     * @return 设置是否成功
     */
    public static boolean setBlockState(FallingBlockEntity entity, BlockState state) {
        try {
            if (blockStateField != null) {
                blockStateField.set(entity, state);
                return true;
            }
        } catch (IllegalAccessException e) {
            CarryOnExtend.LOGGER.error("Unable to set blockState for FallingBlockEntity", e);
        }
        return false;
    }

    /**
     * 设置实体的time字段
     *
     * @param entity 下落方块实体
     * @param time   时间值
     */
    public static void setEntityTime(FallingBlockEntity entity, int time) {
        try {
            if (timeField != null) {
                timeField.set(entity, time);
            }
        } catch (IllegalAccessException e) {
            CarryOnExtend.LOGGER.error("Unable to set the time field of FallingBlockEntity", e);
        }
    }

    /**
     * 放置方块并处理方块实体数据
     *
     * @param entity    自定义下落方块实体
     * @param pos       方块位置
     * @param state     方块状态
     * @param blockData 方块数据
     * @param level     世界
     * @return 放置是否成功
     */
    public static boolean placeBlock(CustomFallingBlockEntity entity, BlockPos pos, BlockState state, CompoundTag blockData, Level level) {
        if (level.setBlock(pos, state, 3)) {
            if (level.getBlockEntity(pos) != null && blockData != null && !blockData.isEmpty()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && blockEntity.getLevel() == null) {
                    blockEntity.setLevel(level);
                }
                blockEntity.loadWithComponents(blockData, level.registryAccess());
                level.sendBlockUpdated(pos, state, state, 3);
                blockEntity.setChanged();
            }
            
            entity.discard();
            return true;
        }
        return false;
    }

    /**
     * 创建带有方块实体数据的物品堆
     *
     * @param state     方块状态
     * @param blockData 方块数据
     * @param pos       方块位置
     * @param level     世界
     * @return 创建的物品堆
     */
    public static ItemStack createItemWithBlockEntityData(BlockState state, CompoundTag blockData, BlockPos pos, Level level) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        
        if (blockData != null && !blockData.isEmpty() && 
            state.getBlock() instanceof EntityBlock entityBlock) {
            
            BlockEntity tempEntity = entityBlock.newBlockEntity(pos, state);
            if (tempEntity != null) {
                tempEntity.setLevel(level);
                tempEntity.loadWithComponents(blockData, level.registryAccess());
                
                // 死妈mojang，给你妈saveToItem方法拆开干嘛，堆石吗？？
                DataComponentMap components = tempEntity.collectComponents();
                
                itemStack.applyComponents(components);
                
            }
        }
        
        return itemStack;
    }
    
    /**
     * 检查实体数据中是否包含特定标签
     * 
     * @param entity 自定义下落方块实体
     * @param key 标签键
     * @return 如果存在该标签则返回true，否则返回false
     */
    public static boolean hasBlockDataTag(CustomFallingBlockEntity entity, String key) {
        CompoundTag blockData = entity.getBlockData();
        return blockData != null && blockData.contains(key);
    }
    
    /**
     * 从实体中获取特定的方块数据标签
     * 
     * @param entity 自定义下落方块实体
     * @param key 标签键
     * @return 如果标签存在则返回该标签，否则返回null
     */
    public static CompoundTag getBlockDataTag(CustomFallingBlockEntity entity, String key) {
        CompoundTag blockData = entity.getBlockData();
        if (blockData != null && blockData.contains(key, CompoundTag.TAG_COMPOUND)) {
            return blockData.getCompound(key);
        }
        return null;
    }
} 