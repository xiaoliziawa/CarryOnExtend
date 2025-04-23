package net.prizowo.carryonextend.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.prizowo.carryonextend.CarryOnExtend;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;

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

                cancelDropField = FallingBlockEntity.class.getDeclaredField("cancelDrop");
                cancelDropField.setAccessible(true);

                timeField = FallingBlockEntity.class.getDeclaredField("time");
                timeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            CarryOnExtend.LOGGER.error("Unable to retrieve the blockState field of FallingBlockEntity", e);
        }
    }

    public CustomFallingBlockEntity(EntityType<CustomFallingBlockEntity> type, Level level) {
        super(type, level);
    }

    public static CustomFallingBlockEntity throwBlock(Level level, double x, double y, double z, BlockState state, CompoundTag blockData, Vec3 motion) {
        CustomFallingBlockEntity entity = new CustomFallingBlockEntity(
                EntityRegistry.CUSTOM_FALLING_BLOCK.get(), level);

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
            this.discard();
            return;
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
                if (this.level().setBlock(pos, blockState, 3)) {
                    if (this.level().getBlockEntity(pos) != null && customData != null && !customData.isEmpty()) {
                        BlockEntity blockEntity = this.level().getBlockEntity(pos);
                        blockEntity.loadWithComponents(customData, this.level().registryAccess());
                        this.level().sendBlockUpdated(pos, blockState, blockState, 3);
                        blockEntity.setChanged();
                    }
                    
                    this.discard();
                }
            } else if (onGround && !handledDrops) {
                if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    NonNullList<ItemStack> containerItems = NonNullList.create();
                    if (customData != null && !customData.isEmpty()) {
                        containerItems = extractItemsFromContainer(customData);

                        customData = removeItemsData(customData.copy());
                    }
                    
                    ItemStack itemStack = new ItemStack(blockState.getBlock());
                    
                    if (customData != null && !customData.isEmpty() && 
                        blockState.getBlock() instanceof EntityBlock entityBlock) {
                        
                            BlockEntity tempEntity = entityBlock.newBlockEntity(pos, blockState);
                            if (tempEntity != null) {
                                tempEntity.loadWithComponents(customData, this.level().registryAccess());
                                
                                tempEntity.saveToItem(itemStack, this.level().registryAccess());
                                
                            }
                    }
                    
                    this.spawnAtLocation(itemStack);
                    
                    for (ItemStack item : containerItems) {
                        if (!item.isEmpty()) {
                            this.spawnAtLocation(item);
                        }
                    }
                    
                    handledDrops = true;
                }
                
                this.discard();
            }
        }
        
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }

    private static <T> void addComponentSafely(DataComponentPatch.Builder patchBuilder,
                                               DataComponentType<?> componentType,
                                               Object componentValue) {
        if (componentValue == null) return;
        
        @SuppressWarnings("unchecked")
            DataComponentType<T> typedComponent =
                (DataComponentType<T>) componentType;
            @SuppressWarnings("unchecked")
            T typedValue = (T) componentValue;
            
            patchBuilder.set(typedComponent, typedValue);
    }

    private NonNullList<ItemStack> extractItemsFromContainer(CompoundTag data) {
        NonNullList<ItemStack> allItems = NonNullList.create();
        
        recursivelyExtractItems(data, allItems);
        
        return allItems;
    }

    private void recursivelyExtractItems(CompoundTag tag, Collection<ItemStack> items) {
        if (tag == null) return;
        
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, 9)) {
                ListTag list = tag.getList(key, 10);
                
                boolean foundItems = false;
                for (int i = 0; i < list.size() && i < 5; i++) {
                    if (list.get(i) instanceof CompoundTag itemTag) {
                        if (looksLikeItemTag(itemTag)) {
                            foundItems = true;
                            break;
                        }
                    }
                }
                
                if (foundItems) {
                    extractItemsFromList(list, items);
                }
            }
        }
        
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, 10)) {
                CompoundTag nestedTag = tag.getCompound(key);
                
                if (looksLikeItemTag(nestedTag)) {
                    ItemStack stack = ItemStack.parseOptional(level().registryAccess(), nestedTag);
                    if (!stack.isEmpty()) {
                        items.add(stack);
                    }
                } else {
                    recursivelyExtractItems(nestedTag, items);
                }
            }
        }
    }
    
    private boolean looksLikeItemTag(CompoundTag tag) {
        if (tag.contains("id") && (tag.contains("Count") || tag.contains("count"))) {
            return true;
        }
        
        String[] itemFields = {"item", "Item", "Slot", "slot"};
        for (String field : itemFields) {
            if (tag.contains(field)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void extractItemsFromList(ListTag itemsList, Collection<ItemStack> items) {
        for (Tag tag : itemsList) {
            if (!(tag instanceof CompoundTag itemTag)) continue;

            ItemStack stack = ItemStack.parseOptional(level().registryAccess(), itemTag);
            if (!stack.isEmpty()) {
                items.add(stack);
                continue;
            }

            for (String key : itemTag.getAllKeys()) {
                if (itemTag.contains(key, 10)) {
                    CompoundTag nestedTag = itemTag.getCompound(key);
                    if (looksLikeItemTag(nestedTag)) {
                        stack = ItemStack.parseOptional(level().registryAccess(), nestedTag);
                        if (!stack.isEmpty()) {
                            items.add(stack);
                            break;
                        }
                    }
                }
            }
        }
    }

    private CompoundTag removeItemsData(CompoundTag data) {
        if (data == null) return new CompoundTag();
        
        String[] commonItemContainers = {"Items", "Inventory", "item_inventory", "inventory", "Container"};
        
        for (String containerTag : commonItemContainers) {
            if (data.contains(containerTag, 9)) {
                data.remove(containerTag);
            }
        }
        
        return data;
    }
}