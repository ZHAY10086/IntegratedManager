package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.lib.gui.event.UpdateScreenEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphAlgorithms;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.ConstrainedGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints.MaxDistanceConstraint;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints.MinDistanceConstraint;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2f;

import java.util.Random;

public class NodeGraphExperiments {

	public static WidgetNodeGraph hexagonGraph() {
		var nodeGraph = new WidgetNodeGraph(GraphAlgorithms.INTEGRATE_THEN_APPLY.get());
		nodeGraph.setSize(1024, 1024);

		long seed = System.currentTimeMillis();
		var random = new Random(seed);

		var diamond = new WidgetItemStack(new ItemStack(Items.DIAMOND, 5), false);
		diamond.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(diamond);

		var emerald = new WidgetItemStack(new ItemStack(Items.EMERALD, 5), false);
		emerald.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(emerald);

		var dirt = new WidgetItemStack(new ItemStack(Items.DIRT, 5), false);
		dirt.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(dirt);

		var gold = new WidgetItemStack(new ItemStack(Items.GOLD_INGOT, 5), false);
		gold.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(gold);

		var iron = new WidgetItemStack(new ItemStack(Items.IRON_INGOT, 5), false);
		iron.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(iron);

		var copper = new WidgetItemStack(new ItemStack(Items.COPPER_INGOT, 5), false);
		copper.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(copper);

		var redstone = new WidgetItemStack(new ItemStack(Items.REDSTONE, 5), false);
		redstone.setPosition(random.nextInt(300)+100, random.nextInt(200));
		nodeGraph.add(redstone);

		WidgetItemStack[] items = {emerald, dirt, gold, iron, copper, redstone};

		nodeGraph.addListener(
			UpdateScreenEvent.class, (event, widget) -> {
				iron.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(iron)));
				diamond.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(diamond)));
				emerald.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(emerald)));
				dirt.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(dirt)));
				gold.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(gold)));
				copper.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(copper)));
				redstone.setTooltipLines(prettyVelocity(nodeGraph.getNodeVelocity(redstone)));

				return WidgetEventResult.CONTINUE_PROCESSING;
			});

		for(WidgetItemStack item : items) {
			nodeGraph.addEdge(createConstrainedEdge(diamond, item));
		}

		for(int i = 0; i < items.length; i++) {
			for(int j = i + 1; j < items.length; j++) {
				nodeGraph.addEdge(createMinDistanceEdge(items[i], items[j], 70f));
			}
		}

		return nodeGraph;
	}


	private static ConstrainedGraphEdge createMinDistanceEdge(WidgetItemStack source, WidgetItemStack target, float distance) {
		return new ConstrainedGraphEdge(source, target)
			.setShouldRender(false)
			.addConstraint(new MinDistanceConstraint(distance, 0.005f));
	}

	private static ConstrainedGraphEdge createConstrainedEdge(WidgetItemStack source, WidgetItemStack target) {
		return createConstrainedEdge(source, target, 60.0f);
	}

	private static ConstrainedGraphEdge createConstrainedEdge(WidgetItemStack source, WidgetItemStack target, float distance) {
		return new ConstrainedGraphEdge(source, target)
			.addConstraint(new MaxDistanceConstraint(distance, 0.005f))
			.addConstraint(new MinDistanceConstraint(distance, 0.005f));
	}

	private static Component prettyVelocity(Vector2f velocity) {
		return Component.literal(String.format("X: %.2f, Y: %.2f", velocity.x, velocity.y));
	}
}
