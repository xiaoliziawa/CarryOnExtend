package net.prizowo.carryonextend.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import net.prizowo.carryonextend.util.BlockRenderUtil;

@OnlyIn(Dist.CLIENT)
public class CustomFallingBlockRenderer extends EntityRenderer<CustomFallingBlockEntity, CustomFallingBlockRenderer.State> {
    private final BlockRenderDispatcher dispatcher;

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(State renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(renderState, poseStack, buffer, packedLight);

        if (!renderState.shouldRender) {
            return;
        }

        poseStack.pushPose();
        try {
            poseStack.translate(-0.5D, 0.0D, -0.5D);

            if (renderState.hasBlockEntity) {
                BlockRenderUtil.renderBlockWithEntity(renderState.entity, renderState.blockState, renderState.level, renderState.renderPos, poseStack, buffer, packedLight, renderState.partialTick);
            } else {
                BlockRenderUtil.renderBlockModel(renderState.blockState, renderState.level, renderState.renderPos, poseStack, buffer, packedLight, false);
            }
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CustomFallingBlockEntity entity, State renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        
        BlockState blockState = entity.getBlockState();
        renderState.blockState = blockState;
        renderState.shouldRender = blockState.getRenderShape() != RenderShape.INVISIBLE;
        renderState.level = entity.level();
        renderState.renderPos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
        renderState.hasBlockEntity = blockState.hasBlockEntity();
        renderState.entity = entity;
        renderState.partialTick = partialTick;
    }

    public static class State extends EntityRenderState {
        public BlockState blockState;
        public boolean shouldRender;
        public Level level;
        public BlockPos renderPos;
        public boolean hasBlockEntity;
        public CustomFallingBlockEntity entity;
        public float partialTick;
    }
}
