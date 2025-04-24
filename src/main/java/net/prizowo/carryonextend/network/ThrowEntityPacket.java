package net.prizowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.prizowo.carryonextend.handler.EntityThrowHandler;

import java.util.function.Supplier;

public class ThrowEntityPacket {
    private final boolean dummy;
    
    public ThrowEntityPacket(boolean dummy) {
        this.dummy = dummy;
    }
    
    public boolean getDummy() {
        return dummy;
    }
    
    public static void encode(ThrowEntityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.dummy);
    }
    
    public static ThrowEntityPacket decode(FriendlyByteBuf buffer) {
        return new ThrowEntityPacket(buffer.readBoolean());
    }
    
    public static void handle(ThrowEntityPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                EntityThrowHandler.throwCarriedEntity(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}