package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.gui.AllElementsReceivedEvent;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetPanningPanel;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphAlgorithms;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.ConstrainedGraphEdge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

public class ManagerPanel extends WidgetPanningPanel {
	WidgetNodeGraph nodeGraph;

	Map<Integer, NetworkPartWidget> partWidgets;
	Map<Integer, VariableFacadeWidget> variableWidgets;

	public Widget addPartWidget(NetworkElementData data) {
		PartData part = data.partData;
		if(partWidgets.containsKey(data.partId)) {
			return null;
		}

		NetworkPartWidget partWidget = new NetworkPartWidget(data);

		Vector2i guessPosition = data.guessPosition(nodeGraph);
		partWidget.setPosition(guessPosition.x, guessPosition.y);
		nodeGraph.add(partWidget);


		partWidgets.put(data.partId, partWidget);

		if(!part.targetStack.isEmpty()) {
			PartTargetWidget partTargetWidget = new PartTargetWidget(part);
			partTargetWidget.setPosition(guessPosition.x + 16, guessPosition.y + 16);
			nodeGraph.add(partTargetWidget);
			ConstrainedGraphEdge edge;
			if(part.writer) {
				edge = ConstrainedGraphEdge.createMaxDistanceEdge(partWidget, partTargetWidget, 24.0f);
			} else {
				edge = ConstrainedGraphEdge.createMaxDistanceEdge(partTargetWidget, partWidget, 24.0f);
			}
			edge.setShouldRender(true);
			edge.setColorSource(ColorHelper.COLOR_ERRORED.getRGB());
			nodeGraph.addEdge(edge);
		}

		return partWidget;
	}

	public Widget addTileWidget(NetworkElementData data) {
		var blockState = Minecraft.getInstance().level.getBlockState(data.position);
		if(blockState.isAir()) {
			return null; // No block at this position
		}

		var blockEntity = Minecraft.getInstance().level.getBlockEntity(data.position);
		if(blockEntity == null) {
			return null; // No tile entity at this position
		}

		var blockStack = new ItemStack(blockState.getBlock());
		WidgetItemStack tileWidget = new WidgetItemStack(blockStack);
		tileWidget.addTooltipElement(
			StringTooltipComponent.gray("Position: " + data.position),
			StringTooltipComponent.gray("Variables: " + data.variables.size())
		);

		Vector2i guessPosition = data.guessPosition(nodeGraph);
		tileWidget.setPosition(guessPosition.x, guessPosition.y);
		nodeGraph.add(tileWidget);

		return tileWidget;
	}

	public ManagerPanel() {
		super();
		this.partWidgets = new HashMap<>();
		this.variableWidgets = new HashMap<>();
		this.nodeGraph = new WidgetNodeGraph(GraphAlgorithms.INTEGRATE_THEN_APPLY.get());
		this.nodeGraph.setSize(1024, 1024);

		this.addListener(
			AllElementsReceivedEvent.class, (event, widget) -> {
				var elements = NetworkData.cache().elementDataList;
				for(NetworkElementData element : elements) {
					Widget elementWidget = null;
					if(element.partData != null) {
						elementWidget = addPartWidget(element);
					} else if(element.tileData != null) {
						//elementWidget = addTileWidget(element);
					}

					for(var variable : element.variables) {
						if(variable.id == -1) {
							continue; // Skip variables that are not used
						}

						if(variableWidgets.containsKey(variable.id)) {
							continue; // Already added
						}


						var guessedPosition = element.guessPosition(nodeGraph);
						VariableFacadeWidget variableWidget = new VariableFacadeWidget(variable);
						variableWidget.setPosition(guessedPosition.x, guessedPosition.y);

						nodeGraph.add(variableWidget);
						variableWidgets.put(variable.id, variableWidget);

						if(elementWidget != null) {
							var edge = ConstrainedGraphEdge.createMaxDistanceEdge(variableWidget, elementWidget, 32.0f);
							edge.setShouldRender(true);
							edge.setColorSource(ColorHelper.COLOR_ORANGE);
							nodeGraph.addEdge(edge);
						}

					}
				}

				for(NetworkElementData element : elements) {
					for(var variable : element.variables) {
						if(variable.id == -1) {
							continue; // Skip variables that are not used
						}

						if(!variableWidgets.containsKey(variable.id)) {
							continue; // Unknown variable, skip it
						}

						Widget variableWidget = variableWidgets.get(variable.id);

						for(Integer referencedId : variable.referencedVariableIds) {
							Widget otherVariableWidget = variableWidgets.get(referencedId);

							if(otherVariableWidget != null && otherVariableWidget != variableWidget) {
								var edge = ConstrainedGraphEdge.createConstrainedEdge(otherVariableWidget, variableWidget, 5.0f);
								edge.setShouldRender(true);
								edge.setColorSource(ColorHelper.COLOR_GREEN);
								nodeGraph.addEdge(edge);
							}
						}

						for(Integer referencedId : variable.referencedPartIds) {
							Widget partWidget = partWidgets.get(referencedId);

							if(partWidget != null) {
								var edge = ConstrainedGraphEdge.createConstrainedEdge(partWidget, variableWidget, 32.0f);
								edge.setShouldRender(true);
								edge.setColorSource(ColorHelper.COLOR_CYAN);
								nodeGraph.addEdge(edge);
							}
						}

					}
				}

				this.centerOnCanvas();

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.add(nodeGraph);
	}

}
