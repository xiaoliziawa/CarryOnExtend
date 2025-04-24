package net.prizowo.carryonextend.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.NetworkHandler;
import net.prizowo.carryonextend.network.ThrowBlockPacket;
import net.prizowo.carryonextend.network.ThrowEntityPacket;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

@Mod.EventBusSubscriber(modid = CarryOnExtend.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (event.getKey() == InputConstants.KEY_Q && player != null && player.isShiftKeyDown()) {
            CarryOnData carry = CarryOnDataManager.getCarryData(player);
            if (carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER)) {
                NetworkHandler.INSTANCE.sendToServer(new ThrowEntityPacket(true));
            }
            else if (carry.isCarrying(CarryOnData.CarryType.BLOCK)) {
                NetworkHandler.INSTANCE.sendToServer(new ThrowBlockPacket(true));
            }
        }
    }
}