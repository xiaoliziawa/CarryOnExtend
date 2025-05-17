package net.prizowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.handler.BlockThrowHandler;
import net.prizowo.carryonextend.handler.EntityThrowHandler;
import org.jetbrains.annotations.NotNull;

public record ThrowPowerPacket(float power, boolean isEntity) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "throw_power");
    public static final Type<ThrowPowerPacket> TYPE = new Type<>(ID);
    
    public static final StreamCodec<FriendlyByteBuf, ThrowPowerPacket> STREAM_CODEC = StreamCodec.<FriendlyByteBuf, ThrowPowerPacket>of(
            (buf, packet) -> {
                buf.writeFloat(packet.power());
                buf.writeBoolean(packet.isEntity());
            },
            buf -> new ThrowPowerPacket(buf.readFloat(), buf.readBoolean())
    );
    
    public static void handle(ThrowPowerPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            net.minecraft.world.entity.player.Player player = ctx.player();
            if (player instanceof ServerPlayer serverPlayer) {
                if (packet.isEntity()) {
                    EntityThrowHandler.throwCarriedEntityWithPower(serverPlayer, packet.power());
                } else {
                    BlockThrowHandler.throwCarriedBlockWithPower(serverPlayer, packet.power());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
} 