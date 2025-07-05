package com.davenonymous.integratedmanager.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientGraphConfig {
	public final ModConfigSpec.BooleanValue SHOW_VARIABLE_STORES;
	public final ModConfigSpec.BooleanValue SHOW_CABLES;

	public static boolean showVariableStores;
	public static boolean showCables;

	public ClientGraphConfig(ModConfigSpec.Builder builder) {
		builder.push("clientgraph");

		SHOW_VARIABLE_STORES = builder
				.comment("Show variable stores in the network graph")
				.translation("integratedmanager.configuration.clientgraph.show_variable_stores")
				.define("showVariableStores", false);

		SHOW_CABLES = builder
				.comment("Show cables in the network graph")
				.translation("integratedmanager.configuration.clientgraph.show_cables")
				.define("showCables", false);

		builder.pop();
	}

	public void load() {
		showVariableStores = SHOW_VARIABLE_STORES.get();
		showCables = SHOW_CABLES.get();
	}
}
