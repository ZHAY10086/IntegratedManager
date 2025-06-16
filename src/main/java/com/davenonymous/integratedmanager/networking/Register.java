package com.davenonymous.integratedmanager.networking;

import com.davenonymous.integratedmanager.IntegratedManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IntegratedManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Register {
	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");

		registrar.playToClient(
			NetworkMasterInfo.TYPE,
			NetworkMasterInfo.STREAM_CODEC,
			NetworkMasterInfo::handleOnClient
		);

		registrar.playToClient(
			NetworkElementInfo.TYPE,
			NetworkElementInfo.STREAM_CODEC,
			NetworkElementInfo::handleOnClient
		);
	}
}
