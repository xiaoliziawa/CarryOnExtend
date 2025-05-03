package net.prizowo.carryonextend.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerThrowPacket {
    private final double x;
    private final double y;
    private final double z;

    public PlayerThrowPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PlayerThrowPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static PlayerThrowPacket decode(FriendlyByteBuf buf) {
        return new PlayerThrowPacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(PlayerThrowPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleOnClient(msg);
        });
        ctx.get().setPacketHandled(true);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PlayerThrowPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player != null) {
            player.setDeltaMovement(packet.x, packet.y, packet.z);
            player.hurtMarked = true;
            player.setOnGround(false);
            
            player.getPersistentData().putLong("ThrowTime", player.level().getGameTime());
            player.getPersistentData().putDouble("ThrowVelocityX", packet.x);
            player.getPersistentData().putDouble("ThrowVelocityY", packet.y);
            player.getPersistentData().putDouble("ThrowVelocityZ", packet.z);
            
            Vec3 currentMotion = player.getDeltaMovement();
            player.setDeltaMovement(currentMotion.x, currentMotion.y + 0.1, currentMotion.z);
            
            player.fallDistance = 0.0f;
        }
    }
} 