package com.davenonymous.integratedmanager.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DebugConfig {
	public final ModConfigSpec.BooleanValue SHOW_VELOCITIES;
	public final ModConfigSpec.BooleanValue SHOW_ALL_EDGES;
	public final ModConfigSpec.BooleanValue SETTLE_GRAPH;
	public final ModConfigSpec.BooleanValue AUTO_ADVANCE_GRAPH;
	public final ModConfigSpec.IntValue SETTLE_INITIAL_STEPS;
	public final ModConfigSpec.IntValue SETTLE_OPTIONAL_STEPS;

	public static boolean showVelocities;
	public static boolean showAllEdges;
	public static boolean settleGraph;
	public static boolean autoAdvanceGraph;
	public static int settleInitialSteps;
	public static int settleOptionalSteps;

	public DebugConfig(ModConfigSpec.Builder builder) {
		builder.push("debug");

		SHOW_VELOCITIES = builder
			.comment("Show velocity vectors.")
			.translation("integratedmanager.configuration.debug.show_velocities")
			.define("showVelocities", false);

		SHOW_ALL_EDGES = builder
			.comment("Show all edges, even if they are simple constraints that are irrelevant for the network.")
			.translation("integratedmanager.configuration.debug.show_all_edges")
			.define("showAllEdges", false);

		AUTO_ADVANCE_GRAPH = builder
			.comment("Automatically advance the graph. If disabled, you will have to manually advance the graph with the Play button in the GUI.")
			.translation("integratedmanager.configuration.debug.auto_advance_graph")
			.define("autoAdvanceGraph", true);

		SETTLE_GRAPH = builder
			.comment("Settle the graph before showing it. This will make sure that all nodes are positioned before rendering starts.")
			.translation("integratedmanager.configuration.debug.settle_graph")
			.define("settleGraph", true);

		SETTLE_INITIAL_STEPS = builder
			.comment("The number of steps to take when settling the graph initially. This is only used if 'settleGraph' is enabled.")
			.translation("integratedmanager.configuration.debug.settle_initial_steps")
			.defineInRange("settleInitialSteps", 1000, 0, 16384);

		SETTLE_OPTIONAL_STEPS = builder
			.comment("The number of optional steps to take when settling the graph. This is only used if 'settleGraph' is enabled.")
			.translation("integratedmanager.configuration.debug.settle_optional_steps")
			.defineInRange("settleOptionalSteps", 2000, 0, 16384);

		builder.pop();
	}

	public void load() {
		showVelocities = SHOW_VELOCITIES.get();
		showAllEdges = SHOW_ALL_EDGES.get();
		settleGraph = SETTLE_GRAPH.get();
		autoAdvanceGraph = AUTO_ADVANCE_GRAPH.get();

		settleInitialSteps = SETTLE_INITIAL_STEPS.get();
		settleOptionalSteps = SETTLE_OPTIONAL_STEPS.get();
	}
}
