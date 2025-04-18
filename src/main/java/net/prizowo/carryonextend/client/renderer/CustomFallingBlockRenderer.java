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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class CustomFallingBlockRenderer extends EntityRenderer<CustomFallingBlockEntity> {
    private final BlockRenderDispatcher dispatcher;
    private static final Map<BlockState, BlockEntity> DUMMY_BLOCK_ENTITY_CACHE = new ConcurrentHashMap<>();
    private float partialTicks;

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CustomFallingBlockEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.partialTicks = partialTicks;
        BlockState blockState = entity.getBlockState();
        if (blockState == null || blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }

        Level level = entity.level();

        poseStack.pushPose();

        BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        poseStack.translate(-0.5, 0.0, -0.5);

        BakedModel model = this.dispatcher.getBlockModel(blockState);

        RandomSource randomSource = RandomSource.create(blockState.getSeed(entity.getStartPos()));

        ModelData modelData = ModelData.EMPTY;

        if (blockState.hasBlockEntity()) {
            renderWithBlockEntity(entity, blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData);
        }
        else if (blockState.getRenderShape() == RenderShape.MODEL) {
            renderModel(blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData, model);
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderModel(BlockState blockState, Level level, BlockPos blockPos,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                             RandomSource randomSource, ModelData modelData, BakedModel model) {
        for (RenderType renderType : model.getRenderTypes(blockState, randomSource, modelData)) {
            this.dispatcher.getModelRenderer().tesselateBlock(
                    level,
                    model,
                    blockState,
                    blockPos,
                    poseStack,
                    buffer.getBuffer(RenderTypeHelper.getMovingBlockRenderType(renderType)),
                    false,
                    randomSource,
                    blockState.getSeed(blockPos),
                    OverlayTexture.NO_OVERLAY,
                    modelData,
                    renderType
            );
        }
    }

    private void renderWithBlockEntity(CustomFallingBlockEntity entity, BlockState blockState,
                                       Level level, BlockPos blockPos, PoseStack poseStack,
                                       MultiBufferSource buffer, int packedLight,
                                       RandomSource randomSource, ModelData modelData) {
        BlockEntityType<?> blockEntityType = null;
        try {
            for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
                if (type.isValid(blockState)) {
                    blockEntityType = type;
                    break;
                }
            }
        } catch (Exception e) {
        }

        if (blockEntityType != null) {
            BlockEntity blockEntity = getDummyBlockEntity(blockState, blockEntityType, blockPos);

            if (blockEntity != null) {
                if (entity.getBlockData() != null && !entity.getBlockData().isEmpty()) {
                    try {
                        blockEntity.loadWithComponents(entity.getBlockData(), level.registryAccess());
                    } catch (Exception e) {
                    }
                }

                BlockEntityRenderer<BlockEntity> blockEntityRenderer = getBlockEntityRenderer(blockEntity);

                if (blockEntityRenderer != null) {
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    try {
                        blockEntityRenderer.render(blockEntity, partialTicks(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
                    } catch (Exception e) {
                        poseStack.popPose();
                        BakedModel model = this.dispatcher.getBlockModel(blockState);
                        renderModel(blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData, model);
                        return;
                    }
                    poseStack.popPose();

                    BakedModel model = this.dispatcher.getBlockModel(blockState);
                    renderModel(blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData, model);
                } else {
                    BakedModel model = this.dispatcher.getBlockModel(blockState);
                    renderModel(blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData, model);
                }
            } else {
                BakedModel model = this.dispatcher.getBlockModel(blockState);
                renderModel(blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData, model);
            }
        } else {
            BakedModel model = this.dispatcher.getBlockModel(blockState);
            renderModel(blockState, level, blockPos, poseStack, buffer, packedLight, randomSource, modelData, model);
        }
    }

    private BlockEntity getDummyBlockEntity(BlockState blockState, BlockEntityType<?> blockEntityType, BlockPos pos) {
        return DUMMY_BLOCK_ENTITY_CACHE.computeIfAbsent(blockState, state -> {
            try {
                return blockEntityType.create(pos, blockState);
            } catch (Exception e) {
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> BlockEntityRenderer<T> getBlockEntityRenderer(T blockEntity) {
        try {
            return (BlockEntityRenderer<T>) Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        } catch (Exception e) {
            return null;
        }
    }

    private float partialTicks() {
        return this.partialTicks;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CustomFallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}