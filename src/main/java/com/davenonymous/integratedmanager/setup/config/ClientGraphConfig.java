package com.davenonymous.integratedmanager.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientGraphConfig {
	public final ModConfigSpec.BooleanValue SHOW_PROXIES;
	public final ModConfigSpec.BooleanValue SHOW_RECIPE_VARIABLES;

	public static boolean showProxies;
	public static boolean showRecipeVariables;

	public ClientGraphConfig(ModConfigSpec.Builder builder) {
		builder.push("clientgraph");

		SHOW_PROXIES = builder
				.comment("Show proxies in the network graph")
				.translation("integratedmanager.configuration.clientgraph.show_proxies")
				.define("showProxies", false);

		SHOW_RECIPE_VARIABLES = builder
				.comment("Show recipe variables in the network graph")
				.translation("integratedmanager.configuration.clientgraph.show_recipe_variables")
				.define("showRecipeVariables", true);

		builder.pop();
	}

	public void load() {
		showProxies = SHOW_PROXIES.get();
		showRecipeVariables = SHOW_RECIPE_VARIABLES.get();
	}
}
