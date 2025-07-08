package com.davenonymous.integratedmanager.lib.gui.widgets;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.lib.gui.GUIHelper;
import com.davenonymous.integratedmanager.lib.gui.event.UpdateScreenEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetDrawEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphAlgorithms;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.IGraphAlgorithm;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.IGraphProvider;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.ConstrainedGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.LineStyle;
import com.davenonymous.integratedmanager.setup.config.DebugConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetNodeGraph extends WidgetPanel implements IGraphProvider {
	private final IGraphAlgorithm algorithm;
	private final List<IGraphEdge> edges = new ArrayList<>();
	private Map<Widget, NodeData> nodeData = new HashMap<>();

	boolean freezeActivity = false;
	int minX = Integer.MAX_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int maxY = Integer.MIN_VALUE;

	public WidgetNodeGraph(IGraphAlgorithm algorithm) {
		this.algorithm = algorithm;

		this.addListener(WidgetDrawEvent.class, ((event, widget) -> {
			if(!freezeActivity && event.type() == WidgetDrawEvent.Type.PRE) {
				this.algorithm.updatePositions(this);
			}
			return WidgetEventResult.CONTINUE_PROCESSING;
		}));

		this.addListener(UpdateScreenEvent.class, (event, widget) -> {
			minX = Integer.MAX_VALUE;
			minY = Integer.MAX_VALUE;
			maxX = Integer.MIN_VALUE;
			maxY = Integer.MIN_VALUE;
			for(Widget node : this.nodes().keySet()) {
				if(node.x < minX) minX = node.x;
				if(node.y < minY) minY = node.y;
				if(node.x + node.width > maxX) maxX = node.x + node.width;
				if(node.y + node.height > maxY) maxY = node.y + node.height;
			}
			return WidgetEventResult.CONTINUE_PROCESSING;
		});
	}

	public WidgetNodeGraph setFreezeActivity(boolean freezeActivity) {
		this.freezeActivity = freezeActivity;
		return this;
	}

	public WidgetNodeGraph() {
		this(GraphAlgorithms.FIXED.get());
	}

	public int getCanvasWidth() {
		return maxX - minX;
	}

	public int getCanvasHeight() {
		return maxY - minY;
	}

	@Override
	public void add(Widget widget) {
		super.add(widget);
		nodeData.put(widget, new NodeData(widget));
	}

	public WidgetNodeGraph addEdge(IGraphEdge edge) {
		var source = edge.source();
		var target = edge.target();
		if(source == null || target == null) {
			// If either source or target is null, skip this edge
			return this;
		}

		if(source == target) {
			// If the source and target are the same, skip this edge
			return this;
		}

		for(var existingEdge : this.edges) {
			if(existingEdge.source() == source && existingEdge.target() == target) {
				// If an edge already exists between these two nodes, merge them
				existingEdge.merge(edge);
				return this;
			}
		}

		this.edges.add(edge);
		return this;
	}

	@Override
	public Map<Widget, NodeData> nodes() {
		return this.nodeData;
	}

	@Override
	public List<IGraphEdge> edges() {
		return this.edges;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		super.draw(guiGraphics, screen);

		if(DebugConfig.showVelocities) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 100); // Ensure nodes are drawn above edges
			for(Widget node : this.nodes().keySet()) {
				Vector2f velocity = this.getNodeVelocity(node);
				if(velocity.length() < 0.025f) {
					// If the velocity is negligible, we can skip drawing the velocity line
					continue;
				}

				int sourceX = node.x + node.width / 2;
				int sourceY = node.y + node.height / 2;
				int targetX = sourceX + (int) (velocity.x * 300);
				int targetY = sourceY + (int) (velocity.y * 300);

				GUIHelper.drawArrowLine(guiGraphics, sourceX, sourceY, targetX, targetY, 0.5f, 0x40CCCCCC);
			}
			guiGraphics.pose().popPose();
		}

		for(IGraphEdge edge : this.edges) {
			var source = edge.source();
			var target = edge.target();
			if(source == null || target == null) {
				// If either source or target is null, skip this edge
				continue;
			}

			int sourceX = source.x + source.width / 2;
			int sourceY = source.y + source.height / 2;
			int targetX = target.x + target.width / 2;
			int targetY = target.y + target.height / 2;

			if(!edge.shouldRender()) {
				if(DebugConfig.showAllEdges) {
					GUIHelper.drawLine(guiGraphics, sourceX, sourceY, targetX, targetY, 0x40CCCCCC);
				}
				continue;
			}

			if(edge.getStyle() != null) {
				switch(edge.getStyle()) {
					case ARROW:
						GUIHelper.drawArrowLine(guiGraphics, sourceX, sourceY, targetX, targetY, edge.getStyle().lineThickness, edge.colorSource());
						break;
					case AA_THIN:
						GUIHelper.drawLine(guiGraphics, sourceX, sourceY, targetX, targetY, edge.colorSource());
						break;
					case MEDIUM:
						GUIHelper.drawFatLine(guiGraphics, sourceX, sourceY, targetX, targetY, edge.getStyle().lineThickness, edge.colorSource());
						break;
					case THIN:
						GUIHelper.drawFatLine(guiGraphics, sourceX, sourceY, targetX, targetY, edge.getStyle().lineThickness, edge.colorSource());
						break;
					case THICK:
						GUIHelper.drawFatLine(guiGraphics, sourceX, sourceY, targetX, targetY, edge.getStyle().lineThickness, edge.colorSource());
						break;
					case INTEGRATED_DYNAMICS_CABLE:
						GUIHelper.drawTiledLine(guiGraphics, sourceX, sourceY, targetX, targetY, edge.getStyle().sprite, edge.colorSource(), edge.getStyle().spacing);
						break;
				}
			} else {
				GUIHelper.drawLine(guiGraphics, sourceX, sourceY, targetX, targetY, 0xFFFF0000);
			}
		}
	}

	public void runIterations(int runs) {
		if (runs <= 0 || freezeActivity) {
			return; // No iterations to run
		}

		for(int i = 0; i < runs; i++) {
			this.algorithm.updatePositions(this);
		}
	}

	public void runUntilSettled(int maxIterations) {
		if (maxIterations <= 0 || freezeActivity) {
			return; // No iterations to run
		}

		int iterations = 0;
		SETTLE: do {
			this.algorithm.updatePositions(this);
			for(Widget node : this.nodes().keySet()) {
				float velocity = this.getNodeVelocity(node).length();
				if(velocity > 0.025f) {
					// If any node has a velocity greater than a small threshold, we consider the graph not settled
					iterations++;
					continue SETTLE;
				}
			}
			break;
		} while (iterations < maxIterations);

		if (iterations >= maxIterations) {
			IntegratedManager.LOGGER.warn("Node graph did not settle after {} iterations", maxIterations);
		} else {
			IntegratedManager.LOGGER.info("Node graph settled after {} iterations", iterations);
		}
	}

	public boolean isFrozen() {
		return this.freezeActivity;
	}
}
