package net.prizowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.prizowo.carryonextend.handler.BlockThrowHandler;

import java.util.function.Supplier;

public class ThrowBlockPacket {
    private final boolean dummy;
    
    public ThrowBlockPacket(boolean dummy) {
        this.dummy = dummy;
    }
    
    public boolean getDummy() {
        return dummy;
    }
    
    public static void encode(ThrowBlockPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.dummy);
    }
    
    public static ThrowBlockPacket decode(FriendlyByteBuf buffer) {
        return new ThrowBlockPacket(buffer.readBoolean());
    }
    
    public static void handle(ThrowBlockPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockThrowHandler.throwCarriedBlock(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}