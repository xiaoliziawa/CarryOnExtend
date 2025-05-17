package net.prizowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.handler.EntityThrowHandler;
import org.jetbrains.annotations.NotNull;

public record ThrowEntityPacket(boolean dummy) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "throw_entity");
    public static final Type<ThrowEntityPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ThrowEntityPacket> STREAM_CODEC = StreamCodec.<FriendlyByteBuf, ThrowEntityPacket>of(
            (buf, packet) -> buf.writeBoolean(packet.dummy()),
            buf -> new ThrowEntityPacket(buf.readBoolean())
    );

    public static void handle(ThrowEntityPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player instanceof ServerPlayer serverPlayer) {
                EntityThrowHandler.throwCarriedEntity(serverPlayer);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
} 