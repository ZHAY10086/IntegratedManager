package com.davenonymous.integratedmanager.lib.gui.widgets;

import com.davenonymous.integratedmanager.lib.gui.GUIHelper;
import com.davenonymous.integratedmanager.lib.gui.event.UpdateScreenEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetDrawEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.AbstractGraphProvider;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphAlgorithms;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.IGraphAlgorithm;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.LineStyle;
import com.davenonymous.integratedmanager.setup.config.DebugConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2f;

public class WidgetNodeGraph extends AbstractGraphProvider {
	int minX = Integer.MAX_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int maxY = Integer.MIN_VALUE;

	public WidgetNodeGraph(IGraphAlgorithm algorithm) {
		super(algorithm);

		this.addListener(WidgetDrawEvent.class, ((event, widget) -> {
			if(!isFrozen() && event.type() == WidgetDrawEvent.Type.PRE) {
				this.runTick();
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

		for(IGraphEdge edge : this.edges()) {
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

			boolean highlight = source.isSelected() && target.isSelected();
			if(highlight && edge.getStyle() != LineStyle.INTEGRATED_DYNAMICS_CABLE) {
				GUIHelper.drawFatLine(guiGraphics, sourceX, sourceY, targetX, targetY, 6, edge.colorSource());
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
}
