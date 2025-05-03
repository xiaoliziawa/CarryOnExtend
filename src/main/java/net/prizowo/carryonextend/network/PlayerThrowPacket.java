package net.prizowo.carryonextend.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.prizowo.carryonextend.CarryOnExtend;
import org.jetbrains.annotations.NotNull;

public record PlayerThrowPacket(double x, double y, double z) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "player_throw");
    public static final Type<PlayerThrowPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PlayerThrowPacket> STREAM_CODEC = StreamCodec.<FriendlyByteBuf, PlayerThrowPacket>of(
            (buf, packet) -> {
                buf.writeDouble(packet.x());
                buf.writeDouble(packet.y());
                buf.writeDouble(packet.z());
            },
            buf -> new PlayerThrowPacket(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            )
    );

    public static void handle(PlayerThrowPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            handleOnClient(packet);
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PlayerThrowPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player != null) {
            player.setDeltaMovement(packet.x(), packet.y(), packet.z());
            player.hurtMarked = true;
            player.setOnGround(false);
            
            player.getPersistentData().putLong("ThrowTime", player.level().getGameTime());
            player.getPersistentData().putDouble("ThrowVelocityX", packet.x());
            player.getPersistentData().putDouble("ThrowVelocityY", packet.y());
            player.getPersistentData().putDouble("ThrowVelocityZ", packet.z());
            
            Vec3 currentMotion = player.getDeltaMovement();
            player.setDeltaMovement(currentMotion.x, currentMotion.y + 0.1, currentMotion.z);
            
            player.fallDistance = 0.0f;
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
} 