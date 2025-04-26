package net.prizowo.carryonextend;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.prizowo.carryonextend.network.ThrowBlockPacket;
import net.prizowo.carryonextend.network.ThrowEntityPacket;
import net.prizowo.carryonextend.network.ThrowPowerPacket;

@Mod.EventBusSubscriber(modid = CarryOnExtend.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CarryOnExtend.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    private static int nextID() {
        return packetId++;
    }
    
    @SubscribeEvent
    public static void registerMessages(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            INSTANCE.registerMessage(nextID(), ThrowEntityPacket.class, 
                    ThrowEntityPacket::encode, 
                    ThrowEntityPacket::decode, 
                    ThrowEntityPacket::handle);
            
            INSTANCE.registerMessage(nextID(), ThrowBlockPacket.class, 
                    ThrowBlockPacket::encode, 
                    ThrowBlockPacket::decode, 
                    ThrowBlockPacket::handle);

            INSTANCE.registerMessage(nextID(), ThrowPowerPacket.class,
                    ThrowPowerPacket::encode,
                    ThrowPowerPacket::decode,
                    ThrowPowerPacket::handle);
        });
    }
}