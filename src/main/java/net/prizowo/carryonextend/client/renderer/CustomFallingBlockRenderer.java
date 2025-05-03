package net.prizowo.carryonextend.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import net.prizowo.carryonextend.util.BlockRenderUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 自定义下落方块实体的渲染器
 */
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
                BlockRenderUtil.renderBlockWithEntity(entity, blockState, level, renderPos, poseStack, buffer, packedLight, partialTicks);
            } else {
                BlockRenderUtil.renderBlockModel(blockState, level, renderPos, poseStack, buffer, packedLight, false);
            }
        } finally {
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CustomFallingBlockEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
