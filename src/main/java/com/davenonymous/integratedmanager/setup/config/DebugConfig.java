package com.davenonymous.integratedmanager.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DebugConfig {
	public final ModConfigSpec.BooleanValue SHOW_VELOCITIES;

	public static boolean showVelocities;

	public DebugConfig(ModConfigSpec.Builder builder) {
		builder.push("debug");

		SHOW_VELOCITIES = builder
			.comment("Show velocity vectors in the manager GUI.")
			.translation("integratedmanager.configuration.debug.show_velocities")
			.define("showVelocities", false);

		builder.pop();
	}

	public void load() {
		showVelocities = SHOW_VELOCITIES.get();
	}
}
