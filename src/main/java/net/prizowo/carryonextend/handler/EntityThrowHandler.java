package net.prizowo.carryonextend.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.prizowo.carryonextend.network.PlayerThrowPacket;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;
import tschipp.carryon.networking.clientbound.ClientboundStartRidingPacket;
import tschipp.carryon.platform.Services;

import java.util.List;


public class EntityThrowHandler {

    private static final float BASE_THROW_POWER = 0.8f;
    private static final float BASE_THROW_UPWARD = 0.2f;
    private static final float MAX_POWER_MULTIPLIER = 2.5f;
    
    // 为玩家实体增加额外的投掷力度
    private static final float PLAYER_THROW_POWER_BONUS = 1.2f;  // 增加水平方向力度
    private static final float PLAYER_THROW_UPWARD_BONUS = 0.6f; // 增加垂直方向力度

    public static void throwCarriedEntity(ServerPlayer player) {
        throwCarriedEntityWithPower(player, 1.0f);
    }
    
    public static void throwCarriedEntityWithPower(ServerPlayer player, float powerFactor) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        
        if (!carry.isCarrying(CarryType.ENTITY) && !carry.isCarrying(CarryType.PLAYER)) {
            return;
        }
        
        Level level = player.level();
        
        float powerMult = 1.0f + (powerFactor * (MAX_POWER_MULTIPLIER - 1.0f));

        float throwPower = BASE_THROW_POWER * powerMult;
        float throwUpward = BASE_THROW_UPWARD * powerMult;
        
        if (carry.isCarrying(CarryType.PLAYER)) {
            Entity passenger = player.getFirstPassenger();
            
            if (passenger != null) {
                Vec3 lookDir = player.getLookAngle();
                Vec3 playerPos = player.position().add(lookDir.multiply(2.0, 0, 2.0)).add(0, 1.5, 0);
                
                float playerThrowPower = throwPower + PLAYER_THROW_POWER_BONUS;
                float playerThrowUpward = throwUpward + PLAYER_THROW_UPWARD_BONUS;
                
                Vec3 velocity = new Vec3(
                    lookDir.x * playerThrowPower,
                    playerThrowUpward,
                    lookDir.z * playerThrowPower
                );
                
                if (level instanceof ServerLevel serverLevel) {
                    List<ServerPlayer> allPlayers = serverLevel.getServer().getPlayerList().getPlayers();
                    for (ServerPlayer serverPlayer : allPlayers) {
                        Services.PLATFORM.sendPacketToPlayer(
                            Constants.PACKET_ID_START_RIDING, 
                            new ClientboundStartRidingPacket(passenger.getId(), false), 
                            serverPlayer
                        );
                    }
                }
                
                // 先清理状态再设置位置和动量
                carry.clear();
                CarryOnDataManager.setCarryData(player, carry);
                
                // 强制清除乘客的状态
                passenger.stopRiding();
                player.ejectPassengers();
                
                // 设置乘客位置
                passenger.setPos(playerPos);
                
                if (passenger instanceof ServerPlayer thrownPlayer) {
                    thrownPlayer.connection.teleport(
                        playerPos.x, playerPos.y, playerPos.z,
                        thrownPlayer.getYRot(), thrownPlayer.getXRot()
                    );
                    
                    PacketDistributor.sendToPlayer(
                        thrownPlayer,
                        new PlayerThrowPacket(velocity.x, velocity.y, velocity.z)
                    );
                } else {
                    passenger.setDeltaMovement(velocity);
                }
                
                float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
                
                player.swing(InteractionHand.MAIN_HAND, true);
                return;
            }
        }
        
        CompoundTag entityNBT = carry.getContentNbt();
        if (entityNBT == null) {
            carry.clear();
            CarryOnDataManager.setCarryData(player, carry);
            return;
        }
        
        Entity entity = carry.getEntity(level);
        Vec3 playerPos = player.position().add(0, 1.0, 0);
        entity.setPos(playerPos);

        
        if (carry.getActiveScript().isPresent()) {
            ScriptEffects effects = carry.getActiveScript().get().scriptEffects();
            String cmd = effects.commandPlace();
            if (!cmd.isEmpty())
                player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), 
                        "/execute as " + player.getGameProfile().getName() + " run " + cmd);
        }
        
        level.addFreshEntity(entity);
        
        Vec3 lookDir = player.getLookAngle();
        entity.setDeltaMovement(lookDir.x * throwPower, throwUpward, lookDir.z * throwPower);
        
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
        
        float pitch = Math.max(0.5f, Math.min(1.8f, 0.8f + powerFactor * 0.8f));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, pitch);
        
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
    }
} 