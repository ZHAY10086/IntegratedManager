package com.davenonymous.integratedmanager.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientGraphConfig {
	public final ModConfigSpec.BooleanValue SHOW_PROXIES;
	public final ModConfigSpec.BooleanValue SHOW_CABLES;

	public static boolean showProxies;
	public static boolean showCables;

	public ClientGraphConfig(ModConfigSpec.Builder builder) {
		builder.push("clientgraph");

		SHOW_PROXIES = builder
				.comment("Show proxies in the network graph")
				.translation("integratedmanager.configuration.clientgraph.show_proxies")
				.define("showProxies", false);

		SHOW_CABLES = builder
				.comment("Show cables in the network graph")
				.translation("integratedmanager.configuration.clientgraph.show_cables")
				.define("showCables", false);

		builder.pop();
	}

	public void load() {
		showProxies = SHOW_PROXIES.get();
		showCables = SHOW_CABLES.get();
	}
}
