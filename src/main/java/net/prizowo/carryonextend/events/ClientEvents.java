package net.prizowo.carryonextend.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.client.PowerThrowHandler;
import net.prizowo.carryonextend.network.ThrowPowerPacket;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null) return;
        
        if (event.getKey() == InputConstants.KEY_Q && event.getAction() == InputConstants.PRESS) {
            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            
            if (carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER) ||
                carry.isCarrying(CarryOnData.CarryType.BLOCK)) {
                
                boolean isEntity = carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER);
                
                float power = PowerThrowHandler.getPowerFactor();
                
                PacketDistributor.sendToServer(new ThrowPowerPacket(power, isEntity));
                
            }
        }
    }
} 