package net.prizowo.carryonextend.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CustomFallingBlockRenderer extends EntityRenderer<CustomFallingBlockEntity> {
    private final BlockRenderDispatcher dispatcher;
    private float partialTicks;

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CustomFallingBlockEntity entity, float entityYaw, float partialTicks,
                      @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        this.partialTicks = partialTicks;

        BlockState blockState = entity.getBlockState();

        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        Level level = entity.level();
        BlockPos renderPos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());

        poseStack.pushPose();
        try {
            poseStack.translate(-0.5D, 0.0D, -0.5D);

            if (blockState.hasBlockEntity()) {
                renderBlockWithEntity(entity, blockState, level, renderPos, poseStack, buffer, packedLight);
            } else {
                renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, false);
            }
        } finally {
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBlockWithEntity(CustomFallingBlockEntity entity, BlockState blockState,
                                     Level level, BlockPos renderPos, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
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
    
    private BlockEntityType<?> findBlockEntityType(BlockState blockState) {
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

    private BlockEntity createBlockEntityInstance(BlockState blockState, BlockEntityType<?> blockEntityType, BlockPos pos) {
        try {
            return blockEntityType.create(pos, blockState);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void renderBlockModel(BlockState blockState, Level level, BlockPos renderPos,
                                PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                boolean useTranslucentMovingBlock) {
        BakedModel model = this.dispatcher.getBlockModel(blockState);
        RandomSource randomSource = RandomSource.create();
        ModelData modelData = ModelData.EMPTY;
        BlockEntity be = level.getBlockEntity(renderPos);
        if (be != null) {
             modelData = be.getModelData();
        }

        for (RenderType renderType : model.getRenderTypes(blockState, randomSource, modelData)) {
            RenderType actualRenderType = useTranslucentMovingBlock ?
                RenderType.translucentMovingBlock() : renderType;

            this.dispatcher.getModelRenderer().tesselateBlock(
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

    private <T extends BlockEntity> BlockEntityRenderer<T> getBlockEntityRenderer(T blockEntity) {
        try {
            return Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CustomFallingBlockEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
