package net.prizowo.carryonextend.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.prizowo.carryonextend.CarryOnExtend;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, value = Dist.CLIENT)
public class PowerThrowHandler {
    private static final int MAX_POWER_STEPS = 10;
    
    private static int powerLevel = MAX_POWER_STEPS;

    public static float getPowerFactor() {
        float minPower = 0.1f;
        if (powerLevel == 1) return minPower;
        
        return minPower + ((float)(powerLevel - 1) / (MAX_POWER_STEPS - 1)) * (1.0f - minPower);
    }
    
    public static void increasePower() {
        if (powerLevel < MAX_POWER_STEPS) {
            powerLevel++;
            playPowerChangeSound();
            displayPowerLevel();
        }
    }
    
    public static void decreasePower() {
        if (powerLevel > 1) {
            powerLevel--;
            playPowerChangeSound();
            displayPowerLevel();
        }
    }
    
    public static void displayPowerLevel() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        
        float percentage = getPowerFactor() * 100;
        Component message = Component.translatable("message." + CarryOnExtend.MOD_ID + ".power_level", 
                                                powerLevel, MAX_POWER_STEPS, (int)percentage);
        
        player.displayClientMessage(message, true);
    }
    
    private static void playPowerChangeSound() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        
        float pitch = 0.5f + (getPowerFactor() * 1.0f);
        player.level().playLocalSound(
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.UI_BUTTON_CLICK.value(),
            SoundSource.PLAYERS,
            0.2f,
            pitch,
            false
        );
    }
    
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        
        if (player == null) return;
        
        if (player.isShiftKeyDown()) {
            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            if (carry.isCarrying(CarryOnData.CarryType.ENTITY) || 
                carry.isCarrying(CarryOnData.CarryType.PLAYER) ||
                carry.isCarrying(CarryOnData.CarryType.BLOCK)) {
                
                if (event.getScrollDeltaY() > 0) {
                    increasePower();
                } else if (event.getScrollDeltaY() < 0) {
                    decreasePower();
                }
                
                event.setCanceled(true);
            }
        }
    }
}