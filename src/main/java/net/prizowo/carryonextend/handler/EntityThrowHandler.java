package net.prizowo.carryonextend.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;


public class EntityThrowHandler {

    private static final float THROW_POWER = 0.8f;
    private static final float THROW_UPWARD = 0.2f;

    public static void throwCarriedEntity(ServerPlayer player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        
        if (!carry.isCarrying(CarryType.ENTITY) && !carry.isCarrying(CarryType.PLAYER)) {
            return;
        }
        
        Level level = player.level();
        
        if (carry.isCarrying(CarryType.PLAYER)) {
            Entity passenger = player.getFirstPassenger();
            player.ejectPassengers();
            carry.clear();
            CarryOnDataManager.setCarryData(player, carry);
            
            if (passenger != null) {
                Vec3 lookDir = player.getLookAngle();
                passenger.setDeltaMovement(lookDir.x * THROW_POWER, THROW_UPWARD, lookDir.z * THROW_POWER);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 0.8F);
            }
            
            player.swing(InteractionHand.MAIN_HAND, true);
            return;
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
        entity.setDeltaMovement(lookDir.x * THROW_POWER, THROW_UPWARD, lookDir.z * THROW_POWER);
        
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
        
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 0.8F);
        
        carry.clear();
        CarryOnDataManager.setCarryData(player, carry);
        player.swing(InteractionHand.MAIN_HAND, true);
        
    }
} 