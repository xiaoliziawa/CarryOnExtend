package net.prizowo.carryonextend.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.prizowo.carryonextend.util.FallingBlockEntityUtil;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;

public class BlockThrowHandler {

    private static final float BASE_THROW_POWER = 0.8f;
    private static final float BASE_THROW_UPWARD = 0.3f;
    private static final float MAX_POWER_MULTIPLIER = 2.5f;

    public static void throwCarriedBlock(ServerPlayer player) {
        throwCarriedBlockWithPower(player, 1.0f);
    }
    
    public static void throwCarriedBlockWithPower(ServerPlayer player, float powerFactor) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        
        if (!carry.isCarrying(CarryType.BLOCK)) {
            return;
        }
        
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        

        BlockState blockState = carry.getBlock();
        Vec3 playerPos = player.getEyePosition().add(player.getLookAngle().scale(0.5));
        BlockPos tempPos = new BlockPos((int)playerPos.x, (int)playerPos.y, (int)playerPos.z);
        BlockEntity blockEntity = carry.getBlockEntity(tempPos, level.registryAccess());
        
        if (carry.getActiveScript().isPresent()) {
            ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
            String cmd = effects.commandPlace();
            if (!cmd.isEmpty())
                player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), 
                        "/execute as " + player.getGameProfile().getName() + " run " + cmd);
        }
        
        float powerMult = 1.0f + (powerFactor * (MAX_POWER_MULTIPLIER - 1.0f));

        Vec3 lookDir = player.getLookAngle();
        Vec3 motion = new Vec3(
            lookDir.x * BASE_THROW_POWER * powerMult,
            BASE_THROW_UPWARD * powerMult,
            lookDir.z * BASE_THROW_POWER * powerMult
        );
        
        CompoundTag blockData = blockEntity != null ? blockEntity.saveWithFullMetadata(level.registryAccess()) : new CompoundTag();
        
        FallingBlockEntityUtil.throwBlock(
            level,
            playerPos.x,
            playerPos.y,
            playerPos.z,
            blockState,
            blockData,
            motion
        );
        
        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
        
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
    }
} 