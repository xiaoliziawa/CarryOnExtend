package net.prizowo.carryonextend.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;

public class BlockRenderUtil {
    
    /**
     * 渲染带有方块实体的方块
     */
    public static void renderBlockWithEntity(CustomFallingBlockEntity entity, BlockState blockState,
                                          Level level, BlockPos renderPos, PoseStack poseStack,
                                          MultiBufferSource buffer, int packedLight, float partialTicks) {
        BlockEntityType<?> blockEntityType = findBlockEntityType(blockState);

        if (blockEntityType == null) {
            renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, true);
            return;
        }

        BlockEntity blockEntity = createBlockEntityInstance(blockState, blockEntityType, renderPos);
        if (blockEntity == null) {
            renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, true);
            return;
        }

        blockEntity.setLevel(level);
        if (entity.getBlockData() != null && !entity.getBlockData().isEmpty()) {
            try {
                blockEntity.loadWithComponents(entity.getBlockData(), level.registryAccess());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (blockState.getRenderShape() == RenderShape.MODEL) {
             renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, true);
        }

        BlockEntityRenderer<BlockEntity> renderer = getBlockEntityRenderer(blockEntity);
        if (renderer != null) {
            poseStack.pushPose();
            try {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                
                renderer.render(blockEntity, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                poseStack.popPose();
            }
        }
    }
    
    /**
     * 查找方块状态对应的方块实体类型
     */
    public static BlockEntityType<?> findBlockEntityType(BlockState blockState) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            try {
                if (type.isValid(blockState)) {
                    return type;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 创建方块实体实例
     */
    public static BlockEntity createBlockEntityInstance(BlockState blockState, BlockEntityType<?> blockEntityType, BlockPos pos) {
        try {
            return blockEntityType.create(pos, blockState);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 渲染方块模型
     */
    public static void renderBlockModel(BlockState blockState, Level level, BlockPos renderPos,
                                     PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                     boolean useTranslucentMovingBlock) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(blockState);
        RandomSource randomSource = RandomSource.create();
        ModelData modelData = ModelData.EMPTY;
        BlockEntity be = level.getBlockEntity(renderPos);
        if (be != null) {
             modelData = be.getModelData();
        }

        for (RenderType renderType : model.getRenderTypes(blockState, randomSource, modelData)) {
            RenderType actualRenderType = useTranslucentMovingBlock ?
                RenderType.translucentMovingBlock() : renderType;

            dispatcher.getModelRenderer().tesselateBlock(
                level,
                model,
                blockState,
                renderPos,
                poseStack,
                buffer.getBuffer(actualRenderType),
                false,
                randomSource,
                blockState.getSeed(renderPos),
                OverlayTexture.NO_OVERLAY,
                modelData,
                actualRenderType
            );
        }
    }

    /**
     * 获取方块实体的渲染器
     */
    public static <T extends BlockEntity> BlockEntityRenderer<T> getBlockEntityRenderer(T blockEntity) {
        try {
            return Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 