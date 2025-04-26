package net.prizowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.prizowo.carryonextend.handler.BlockThrowHandler;
import net.prizowo.carryonextend.handler.EntityThrowHandler;

import java.util.function.Supplier;

public class ThrowPowerPacket {
    private final float power;
    private final boolean isEntity;
    
    public ThrowPowerPacket(float power, boolean isEntity) {
        this.power = power;
        this.isEntity = isEntity;
    }
    
    public static void encode(ThrowPowerPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.power);
        buf.writeBoolean(packet.isEntity);
    }
    
    public static ThrowPowerPacket decode(FriendlyByteBuf buf) {
        return new ThrowPowerPacket(buf.readFloat(), buf.readBoolean());
    }
    
    public static void handle(ThrowPowerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (packet.isEntity) {
                    EntityThrowHandler.throwCarriedEntityWithPower(player, packet.power);
                } else {
                    BlockThrowHandler.throwCarriedBlockWithPower(player, packet.power);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public float getPower() {
        return power;
    }
    
    public boolean isEntity() {
        return isEntity;
    }
} 