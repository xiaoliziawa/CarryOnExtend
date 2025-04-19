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
import net.prizowo.carryonextend.registry.CustomFallingBlockEntity;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;

public class BlockThrowHandler {

    private static final float THROW_POWER = 0.8f;
    private static final float THROW_UPWARD = 0.3f;

    public static void throwCarriedBlock(ServerPlayer player) {
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
        
        Vec3 lookDir = player.getLookAngle();
        Vec3 motion = new Vec3(
            lookDir.x * THROW_POWER,
            THROW_UPWARD,
            lookDir.z * THROW_POWER
        );
        
        CompoundTag blockData = blockEntity != null ? blockEntity.saveWithFullMetadata(level.registryAccess()) : new CompoundTag();
        
        CustomFallingBlockEntity.throwBlock(
            level,
            playerPos.x,
            playerPos.y,
            playerPos.z,
            blockState,
            blockData,
            motion
        );
        
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 0.8F);
        
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
    }
} 