package net.prizowo.carryonextend;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.prizowo.carryonextend.network.ThrowBlockPacket;
import net.prizowo.carryonextend.network.ThrowEntityPacket;
import net.prizowo.carryonextend.network.ThrowPowerPacket;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CarryOnExtend.MOD_ID)
                .versioned("1.0.0");

        registrar.playToServer(
                ThrowEntityPacket.TYPE,
                ThrowEntityPacket.STREAM_CODEC,
                ThrowEntityPacket::handle
        );
        registrar.playToServer(
                ThrowBlockPacket.TYPE,
                ThrowBlockPacket.STREAM_CODEC,
                ThrowBlockPacket::handle
        );
        registrar.playToServer(
                ThrowPowerPacket.TYPE,
                ThrowPowerPacket.STREAM_CODEC,
                ThrowPowerPacket::handle
        );
    }
}