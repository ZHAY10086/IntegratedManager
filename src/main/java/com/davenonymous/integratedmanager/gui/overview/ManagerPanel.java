package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.AllElementsReceivedEvent;
import com.davenonymous.integratedmanager.gui.NodeUpdateEvent;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.integrated.common.TileData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetPanningPanel;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphAlgorithms;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphHelpers;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.ConstrainedGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.LineStyle;
import com.davenonymous.integratedmanager.setup.config.ClientGraphConfig;
import com.davenonymous.integratedmanager.setup.config.DebugConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.joml.Vector2f;

import java.util.*;

public class ManagerPanel extends WidgetPanningPanel {
	WidgetNodeGraph nodeGraph;

	Map<BlockPos, NetworkTileWidget> tileWidgets;
	Map<Integer, NetworkPartWidget> partWidgets;
	Map<Integer, VariableFacadeWidget> variableWidgets;
	Map<Integer, NetworkTileWidget> proxyWidgets;
	Map<Integer, List<NodeWidget<NetworkElementData>>> elementsBySupplyChannelId;

	Map<Integer, List<NodeWidget<NetworkElementData>>> elementsByItemChannelId;
	Map<Integer, List<NodeWidget<NetworkElementData>>> elementsByFluidChannelId;
	Map<Integer, List<NodeWidget<NetworkElementData>>> elementsByEnergyChannelId;

	boolean fullDataReceived = false;

	public Widget addPartWidget(NetworkElementData data, int initialX, int initialY) {
		PartData part = data.partData;
		if(partWidgets.containsKey(data.partId)) {
			return null;
		}

		NetworkPartWidget partWidget = new NetworkPartWidget(data);

		partWidget.setPosition(initialX, initialY);
		nodeGraph.add(partWidget);


		partWidgets.put(data.partId, partWidget);
		elementsBySupplyChannelId.computeIfAbsent(data.channelId, k -> new ArrayList<>()).add(partWidget);

		if(part.activeAspectProperties.containsKey("gui.integratedtunnels.partsettings.channel.interface")) {
			int interfaceChannel = Integer.parseInt(part.activeAspectProperties.get("gui.integratedtunnels.partsettings.channel.interface").stringValue);
			if(part.onItemChannel) {
				elementsByItemChannelId.computeIfAbsent(interfaceChannel, k -> new ArrayList<>()).add(partWidget);
			} else if(part.onFluidChannel) {
				elementsByFluidChannelId.computeIfAbsent(interfaceChannel, k -> new ArrayList<>()).add(partWidget);
			} else if(part.onEnergyChannel) {
				elementsByEnergyChannelId.computeIfAbsent(interfaceChannel, k -> new ArrayList<>()).add(partWidget);
			}
		}

		if(part.activeAspectProperties.containsKey("aspect.aspecttypes.integrateddynamics.integer.channel")) {
			int interfaceChannel = Integer.parseInt(part.activeAspectProperties.get("aspect.aspecttypes.integrateddynamics.integer.channel").stringValue);
			if(part.onItemChannel) {
				elementsByItemChannelId.computeIfAbsent(interfaceChannel, k -> new ArrayList<>()).add(partWidget);
			} else if(part.onFluidChannel) {
				elementsByFluidChannelId.computeIfAbsent(interfaceChannel, k -> new ArrayList<>()).add(partWidget);
			} else if(part.onEnergyChannel) {
				elementsByEnergyChannelId.computeIfAbsent(interfaceChannel, k -> new ArrayList<>()).add(partWidget);
			}
		}


		if(!part.targetStack.isEmpty()) {
			PartTargetWidget partTargetWidget = new PartTargetWidget(part);
			partTargetWidget.setPosition(initialX + 16, initialY + 16);
			nodeGraph.add(partTargetWidget);
			ConstrainedGraphEdge edge;
			if(part.writer) {
				edge = ConstrainedGraphEdge.createMaxDistanceEdge(partWidget, partTargetWidget, 24.0f);
			} else {
				edge = ConstrainedGraphEdge.createMaxDistanceEdge(partTargetWidget, partWidget, 24.0f);
			}
			edge.setShouldRender(true);
			edge.setStyle(LineStyle.ARROW);
			edge.setColorSource(ColorHelper.COLOR_ERRORED.getRGB());
			nodeGraph.addEdge(edge);
		}

		return partWidget;
	}

	public Widget addTileWidget(NetworkElementData data, int initialX, int initialY) {
		TileData tileData = data.tileData;
		if(tileData == null || data.position == null) {
			return null; // No tile data or position
		}

		if(tileWidgets.containsKey(data.position)) {
			return null;
		}

		if(!ClientGraphConfig.showProxies && tileData.blockEntityClass.equals(("BlockEntityProxy"))) {
			// This tile entity is a proxy, we do not want to show it in the graph.
			// Proxies are only used to connect variables to other parts or tiles.
			return null;
		}

		if(tileData.blockEntityClass.equals("BlockEntityVariablestore")) {
			if(data.variables.stream().noneMatch(VariableData::isUnused)) {
				// This tile entity is a variable store, but it has no variables that are not referenced by other parts or tiles.
				// We do not want to show this tile entity in the graph, as it is not useful.
				return null;
			}
		}

		var blockState = Minecraft.getInstance().level.getBlockState(data.position);
		if(blockState.isAir()) {
			return null; // No block at this position
		}

		var blockEntity = Minecraft.getInstance().level.getBlockEntity(data.position);
		if(blockEntity == null) {
			return null; // No tile entity at this position
		}

		NetworkTileWidget tileWidget = new NetworkTileWidget(data);

		tileWidget.addTooltipElement(
			StringTooltipComponent.gray("Position: " + data.position),
			StringTooltipComponent.gray("Variables: " + data.variables.size())
		);

		tileWidget.setPosition(initialX, initialY);
		nodeGraph.add(tileWidget);

		tileWidgets.put(data.position, tileWidget);
		elementsBySupplyChannelId.computeIfAbsent(data.channelId, k -> new ArrayList<>()).add(tileWidget);

		if(tileData.proxyId >= 0) {
			proxyWidgets.put(tileData.proxyId, tileWidget);
		}

		return tileWidget;
	}

	public ManagerPanel() {
		super();
		this.partWidgets = new HashMap<>();
		this.variableWidgets = new HashMap<>();
		this.tileWidgets = new HashMap<>();
		this.proxyWidgets = new HashMap<>();
		this.elementsBySupplyChannelId = new HashMap<>();
		this.elementsByItemChannelId = new HashMap<>();
		this.elementsByFluidChannelId = new HashMap<>();
		this.elementsByEnergyChannelId = new HashMap<>();

		this.nodeGraph = new WidgetNodeGraph(GraphAlgorithms.INTEGRATE_THEN_APPLY.get());
		this.nodeGraph.setSize(1024, 1024);

		this.addListener(NodeUpdateEvent.class, ((event, widget) -> {
			if(!this.fullDataReceived) {
				return WidgetEventResult.CONTINUE_PROCESSING;
			}

			NetworkElementData data = event.data();
			for(var variableData : data.variables) {
				if(variableData.id == -1) {
					continue;
				}

				if(!variableWidgets.containsKey(variableData.id)) {
					continue;
				}

				VariableFacadeWidget variableWidget = variableWidgets.get(variableData.id);

				// TODO: Update the variable widget with new data

			}

			return WidgetEventResult.CONTINUE_PROCESSING;
		}));

		this.addListener(
			AllElementsReceivedEvent.class, (event, widget) -> {
				if(this.fullDataReceived) {
					return WidgetEventResult.CONTINUE_PROCESSING; // Already processed
				}

				this.fullDataReceived = true;
				int elementSize = 48;
				Vector2f center = new Vector2f(
					(nodeGraph.width / 2.0f),
					(nodeGraph.height / 2.0f)
				);

				var spiral = new GraphHelpers.SpiralIterator(elementSize, center);

				var elements = NetworkData.cache().elementDataList;
				var sortedElements = elements.stream().sorted(Comparator.comparing(networkElementData -> networkElementData.position)).toList();
				for(NetworkElementData element : sortedElements) {
					Widget elementWidget = null;
					if(element.partData != null) {
						Vector2f bestPos = spiral.next();
						elementWidget = addPartWidget(element, (int) bestPos.x, (int) bestPos.y);
					} else if(element.tileData != null) {
						Vector2f bestPos = spiral.next();
						elementWidget = addTileWidget(element, (int) bestPos.x, (int) bestPos.y);
					}

 					for(var variable : element.variables) {
						if(variable.id == -1) {
							continue; // Skip variables that are not used
						}

						if(variableWidgets.containsKey(variable.id)) {
							continue; // Already added
						}


						VariableFacadeWidget variableWidget = new VariableFacadeWidget(variable);
						variableWidget.setPosition(spiral.next());


						nodeGraph.add(variableWidget);
						variableWidgets.put(variable.id, variableWidget);

						if(elementWidget != null) {
							var edge = ConstrainedGraphEdge.createMaxDistanceEdge(variableWidget, elementWidget, 32.0f);
							edge.setShouldRender(true);
							edge.setStyle(LineStyle.ARROW);
							edge.setColorSource(ColorHelper.COLOR_ORANGE);

							if(elementWidget instanceof NetworkTileWidget networkTileWidget && networkTileWidget.getValue().tileData.blockEntityClass.equals("BlockEntityVariablestore")) {
								if(variable.hasNoReferences() && variable.isNotBeingReferenced()) {
									edge.setStyle(LineStyle.AA_THIN);
									edge.setColorSource(ColorHelper.COLOR_DISABLED.getRGB());
								} else {
									continue;
								}
							}
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
								edge.setStyle(LineStyle.ARROW);
								edge.setColorSource(ColorHelper.COLOR_GREEN);
								nodeGraph.addEdge(edge);
							}
						}

						for(Integer referencedId : variable.referencedPartIds) {
							Widget partWidget = partWidgets.get(referencedId);

							if(partWidget != null) {
								var edge = ConstrainedGraphEdge.createConstrainedEdge(partWidget, variableWidget, 32.0f);
								edge.setShouldRender(true);
								edge.setStyle(LineStyle.ARROW);
								edge.setColorSource(ColorHelper.COLOR_CYAN);
								nodeGraph.addEdge(edge);
							}
						}

						if(variable.proxyId >= 0) {
							NetworkTileWidget proxyWidget = proxyWidgets.get(variable.proxyId);
							if(proxyWidget != null) {
								var edge = ConstrainedGraphEdge.createConstrainedEdge(proxyWidget, variableWidget, 32.0f);
								edge.setShouldRender(true);
								edge.setStyle(LineStyle.ARROW);
								edge.setColorSource(ColorHelper.COLOR_PURPLE);
								nodeGraph.addEdge(edge);
							} else {
								VariableData proxiedVariable = NetworkData.cache().variableDataByProxyId.get(variable.proxyId);
								if(proxiedVariable == null) {
									IntegratedManager.LOGGER.warn("Proxy variable with ID {} not found in cache, this is likely a bug.", variable.proxyId);
									continue; // Skip if the proxied variable is not found
								}
								VariableFacadeWidget proxyVariableWidget = variableWidgets.get(proxiedVariable.id);
								if(proxyVariableWidget != null) {
									var edge = ConstrainedGraphEdge.createConstrainedEdge(proxyVariableWidget, variableWidget, 32.0f);
									edge.setShouldRender(true);
									edge.setStyle(LineStyle.ARROW);
									edge.setColorSource(ColorHelper.COLOR_PURPLE);
									nodeGraph.addEdge(edge);
								} else {
									IntegratedManager.LOGGER.warn("Proxy variable widget for ID {} not found, this is likely a bug.", proxiedVariable.id);
								}
							}
						}

					}
				}

				if(ClientGraphConfig.showCables) {
					for(int channelId : elementsBySupplyChannelId.keySet()) {
						List<NodeWidget<NetworkElementData>> channelElements = elementsBySupplyChannelId.get(channelId);
						if(channelElements.size() < 2) {
							continue; // No edges to draw
						}

						for(int i = 0; i < channelElements.size() - 1; i++) {
							NodeWidget<NetworkElementData> from = channelElements.get(i);
							NodeWidget<NetworkElementData> to = channelElements.get(i + 1);

							ConstrainedGraphEdge edge = ConstrainedGraphEdge.createConstrainedEdge(from, to, 32.0f);
							edge.setStyle(LineStyle.INTEGRATED_DYNAMICS_CABLE);
							edge.setShouldRender(true);
							edge.setColorSource(0xFFFFFFFF);
							nodeGraph.addEdge(edge);
						}
					}
				}

				if(DebugConfig.settleGraph) {
					nodeGraph.runIterations(DebugConfig.settleInitialSteps);
					nodeGraph.runUntilSettled(DebugConfig.settleOptionalSteps);
				}

				this.freezeActivity(!DebugConfig.autoAdvanceGraph);
				this.centerOnCanvas();

				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);

		this.add(nodeGraph);
	}

	public ManagerPanel freezeActivity(boolean state) {
		this.nodeGraph.setFreezeActivity(state);
		return this;
	}

	public boolean isFrozen() {
		return this.nodeGraph.isFrozen();
	}

}
