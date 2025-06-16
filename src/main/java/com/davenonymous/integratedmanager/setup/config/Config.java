package com.davenonymous.integratedmanager.setup.config;

import com.davenonymous.integratedmanager.IntegratedManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = IntegratedManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
	public static final ModConfigSpec COMMON_SPEC;
	public static final ModConfigSpec CLIENT_SPEC;
	public static final DebugConfig Debug;

	static {
		ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
		COMMON_SPEC = commonBuilder.build();

		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		Debug = new DebugConfig(builder);
		CLIENT_SPEC = builder.build();

	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		if(event.getConfig().getSpec() == COMMON_SPEC) {
		} else if(event.getConfig().getSpec() == CLIENT_SPEC) {
			Debug.load();
		}
	}
}
