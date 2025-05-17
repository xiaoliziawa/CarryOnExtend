package net.prizowo.carryonextend.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.prizowo.carryonextend.CarryOnExtend;
import net.prizowo.carryonextend.handler.BlockThrowHandler;
import org.jetbrains.annotations.NotNull;

public record ThrowBlockPacket(boolean dummy) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "throw_block");
    public static final Type<ThrowBlockPacket> TYPE = new Type<>(ID);
    
    public static final StreamCodec<FriendlyByteBuf, ThrowBlockPacket> STREAM_CODEC = StreamCodec.<FriendlyByteBuf, ThrowBlockPacket>of(
            (buf, packet) -> buf.writeBoolean(packet.dummy()),
            buf -> new ThrowBlockPacket(buf.readBoolean())
    );
    
    public static void handle(ThrowBlockPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            net.minecraft.world.entity.player.Player player = ctx.player();
            if (player instanceof ServerPlayer serverPlayer) {
                BlockThrowHandler.throwCarriedBlock(serverPlayer);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
} 