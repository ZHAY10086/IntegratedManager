package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.AllElementsReceivedEvent;
import com.davenonymous.integratedmanager.gui.NodeUpdateEvent;
import com.davenonymous.integratedmanager.gui.search.ElementSearchables;
import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.common.*;
import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import com.davenonymous.integratedmanager.lib.gui.event.MouseClickEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetPanningPanel;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.AbstractGraphProvider;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphAlgorithms;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.GraphHelpers;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.ConstrainedGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.LineStyle;
import com.davenonymous.integratedmanager.setup.config.ClientGraphConfig;
import com.davenonymous.integratedmanager.setup.config.DebugConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
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
	Map<Integer, CableIntersectionWidget> cableIntersectionWidgets;
	Map<Integer, OmniIntersectionWidget> omniIntersectionWidgets;
	Map<BlockPos, PartTargetWidget> partTargetWidgets = new HashMap<>();

	Widget selectedWidget = null;
	List<Widget> selectedDescendants;
	List<Widget> selectedAncestors;

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
			if(!this.partTargetWidgets.containsKey(part.targetPos)) {
				PartTargetWidget partTargetWidget = new PartTargetWidget(part);
				partTargetWidget.setPosition(initialX + 16, initialY + 16);
				nodeGraph.add(partTargetWidget);

				partTargetWidget.addListener(MouseClickEvent.class, (event1, widget1) -> {
					if(event1.button != 0 || getGUI().isShiftDown()) { // Left click + shift (scancode=340)
						return WidgetEventResult.CONTINUE_PROCESSING;
					}

					if(selectedWidget != null && selectedWidget.equals(widget1)) {
						deselectAll();
						return WidgetEventResult.HANDLED;
					}

					deselectAll();
					selectedWidget = widget1;
					widget1.setSelected(true);

					selectedDescendants = nodeGraph.getDescendants(selectedWidget);
					selectedAncestors = nodeGraph.getAncestors(selectedWidget);
					selectedDescendants.forEach(descendent -> descendent.setSelected(true));
					selectedAncestors.forEach(ancestor -> ancestor.setSelected(true));

					return WidgetEventResult.HANDLED;
				});

				this.partTargetWidgets.put(part.targetPos, partTargetWidget);
			}

			PartTargetWidget partTargetWidget = this.partTargetWidgets.get(part.targetPos);
			ConstrainedGraphEdge edge;
			if(part.writer) {
				edge = ConstrainedGraphEdge.createMaxDistanceEdge(partWidget, partTargetWidget, 64f);
			} else {
				edge = ConstrainedGraphEdge.createMaxDistanceEdge(partTargetWidget, partWidget, 64f);
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

		if(!ClientGraphConfig.showProxies && tileData.blockEntityClass.equals(("BlockEntityHttp"))) {
			// This tile entity is a proxy, we do not want to show it in the graph.
			// Proxies are only used to connect variables to other parts or tiles.
			//return null;
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
		this.selectedDescendants = new ArrayList<>();
		this.selectedAncestors = new ArrayList<>();
		this.cableIntersectionWidgets = new HashMap<>();
		this.omniIntersectionWidgets = new HashMap<>();
		this.partTargetWidgets = new HashMap<>();

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
				int elementSize = 96;
				Vector2f center = new Vector2f(
					(nodeGraph.width / 2.0f),
					(nodeGraph.height / 2.0f)
				);

				var spiral = new GraphHelpers.SpiralIterator(elementSize, center);

				Map<Integer, CableIntersectionWidget> cableIntersections = new HashMap<>();
				for(int pathNodeId : NetworkData.cache().paths.keySet()) {
					var cableWidget = new CableIntersectionWidget(pathNodeId);
					cableWidget.setPosition(spiral.next());
					List<NetworkElementData> elementDatas = NetworkData.cache().elementDataByPathId.get(pathNodeId);
					if(elementDatas != null && elementDatas.size() == 1) {
						var element = elementDatas.getFirst();
						if(element.tileData != null) {
							if(element.tileData.blockEntityClass.equals("BlockEntityVariablestore")) {
								cableWidget.setIcon(new ItemStack(RegistryEntries.BLOCK_VARIABLE_STORE.get()));
								cableWidget.setLabelTranslationKey("block.integrateddynamics.variablestore");
							} else if(element.tileData.blockEntityClass.equals("BlockEntityProxy")) {
								cableWidget.setValue(element.tileData.proxyId);
								cableWidget.setIcon(new ItemStack(RegistryEntries.BLOCK_PROXY.get()));
								cableWidget.setLabelTranslationKey("block.integrateddynamics.proxy");
							} else if(element.tileData.blockEntityClass.equals("BlockEntityHttp")) {
								//cableWidget.setValue(element.tileData.proxyId);
								//cableWidget.setIcon(new ItemStack(RegistryEntries.BLOCK_PROXY.get()));
								//cableWidget.setLabelTranslationKey("block.integratedrest.http");
							}
							cableWidget.updateTooltip();
						}
					}

					nodeGraph.add(cableWidget);
					cableIntersections.put(pathNodeId, cableWidget);
					cableIntersectionWidgets.put(pathNodeId, cableWidget);
				}

				for(int pathNodeId : NetworkData.cache().paths.keySet()) {
					var connections = NetworkData.cache().paths.get(pathNodeId);
					CableIntersectionWidget intersectionWidget = cableIntersections.get(pathNodeId);
					for(Integer neighborId : connections.keySet()) {
						IntegratedConnectionType connectionType = connections.get(neighborId);
						if(connectionType == null) {
							continue; // No connection type, skip
						}

						CableIntersectionWidget neighborWidget = cableIntersections.get(neighborId);
						if(neighborWidget == null) {
							continue; // No neighbor widget, skip
						}

						float edgeDistance = 50.0f;
						if(connectionType == IntegratedConnectionType.MONO) {
							edgeDistance = 96f;
						}
						ConstrainedGraphEdge edge = ConstrainedGraphEdge.createMaxDistanceEdge(intersectionWidget, neighborWidget, edgeDistance);
						edge.setShouldRender(true);
						edge.setStyle(LineStyle.INTEGRATED_DYNAMICS_CABLE);
						edge.setColorSource(0xFFFFFFFF);
						if(connectionType == IntegratedConnectionType.MONO) {
							edge.setStyle(LineStyle.INTEGRATED_DYNAMICS_MONO);
						}
						nodeGraph.addEdge(edge);
					}
				}

				var elements = NetworkData.cache().elementDataList;
				var sortedElements = elements.stream().sorted(Comparator.comparing(networkElementData -> networkElementData.position)).toList();
				for(NetworkElementData element : sortedElements) {
					if(element.partData != null && element.partData.omniId != -1) {
						if(!omniIntersectionWidgets.containsKey(element.partData.omniId)) {
							OmniIntersectionWidget omniIntersectionWidget = new OmniIntersectionWidget(element.partData.omniId);
							omniIntersectionWidget.setPosition(spiral.next());
							omniIntersectionWidgets.put(element.partData.omniId, omniIntersectionWidget);
							nodeGraph.add(omniIntersectionWidget);
						}
					}
				}



				for(NetworkElementData element : sortedElements) {
					Widget elementWidget = null;
					if(element.partData != null && !element.partData.partClassName.equals("PartTypeConnectorMonoDirectional")) {
						Vector2f bestPos = spiral.next();
						elementWidget = addPartWidget(element, (int) bestPos.x, (int) bestPos.y);
					} else if(element.tileData != null) {
						Vector2f bestPos = spiral.next();
						elementWidget = addTileWidget(element, (int) bestPos.x, (int) bestPos.y);
					}

					if(element.partData != null && element.partData.omniId != -1) {
						OmniIntersectionWidget omniIntersectionWidget = omniIntersectionWidgets.get(element.partData.omniId);
						if(omniIntersectionWidget != null && elementWidget != null) {
							var edge = ConstrainedGraphEdge.createMaxDistanceEdge(omniIntersectionWidget, elementWidget, 192.0f);
							edge.setShouldRender(true);
							edge.setStyle(LineStyle.INTEGRATED_DYNAMICS_MONO);
							edge.setColorSource(ColorHelper.COLOR_PURPLE);
							nodeGraph.addEdge(edge);
						}
					}

					if(elementWidget != null) {
						if(element.pathId != -1 && nodeGraph instanceof AbstractGraphProvider nodeGraph) {
							CableIntersectionWidget cableIntersectionWidget = cableIntersectionWidgets.get(element.pathId);

							var edge = ConstrainedGraphEdge.createConstrainedEdge(cableIntersectionWidget, elementWidget, 32.0f);
							edge.setShouldRender(true);
							edge.setStyle(LineStyle.INTEGRATED_DYNAMICS_CABLE);
							edge.setColorSource(0x80FFFFFF);
							nodeGraph.addEdge(edge);
						}


						element.updateSearchIndex(elementWidget);
						elementWidget.addListener(MouseClickEvent.class, (event1, widget1) -> {
							if(event1.button != 0 || getGUI().isShiftDown()) { // Left click + shift (scancode=340)
								return WidgetEventResult.CONTINUE_PROCESSING;
							}

							if(selectedWidget != null && selectedWidget.equals(widget1)) {
								deselectAll();
								return WidgetEventResult.HANDLED;
							}

							deselectAll();
							selectedWidget = widget1;
							widget1.setSelected(true);

							selectedDescendants = nodeGraph.getDescendants(selectedWidget);
							selectedAncestors = nodeGraph.getAncestors(selectedWidget);
							selectedDescendants.forEach(descendent -> descendent.setSelected(true));
							selectedAncestors.forEach(ancestor -> ancestor.setSelected(true));

							return WidgetEventResult.HANDLED;
						});
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
						variableWidget.addListener(MouseClickEvent.class, (event1, widget1) -> {
							if(event1.button != 0 || getGUI().isShiftDown()) { // Left click + shift (scancode=340)
								return WidgetEventResult.CONTINUE_PROCESSING;
							}

							if(selectedWidget != null && selectedWidget.equals(variableWidget)) {
								deselectAll();
								return WidgetEventResult.HANDLED;
							}

							deselectAll();
							selectedWidget = variableWidget;
							variableWidget.setSelected(true);

							selectedDescendants = nodeGraph.getDescendants(selectedWidget);
							selectedAncestors = nodeGraph.getAncestors(selectedWidget);
							selectedDescendants.forEach(descendent -> descendent.setSelected(true));
							selectedAncestors.forEach(ancestor -> ancestor.setSelected(true));

							return WidgetEventResult.HANDLED;
						});

						variable.updateSearchIndex(variableWidget);
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

	public ManagerPanel deselectAll() {
		if(selectedWidget != null) {
			selectedWidget.setSelected(false);
			selectedWidget = null;
		}

		for(Widget descendent : selectedDescendants) {
			descendent.setSelected(false);
		}
		selectedDescendants.clear();

		for(Widget ancestor : selectedAncestors) {
			ancestor.setSelected(false);
		}
		selectedAncestors.clear();

		return this;
	}

	public ManagerPanel freezeActivity(boolean state) {
		this.nodeGraph.setFreezeActivity(state);
		return this;
	}

	public boolean isFrozen() {
		return this.nodeGraph.isFrozen();
	}

	public void runSearch(String rawQueryString, boolean searchCompareCase, boolean searchWithRegex, Set<ElementSearchables> includeInSearch) {
		String queryString = rawQueryString.trim();
		if (!searchCompareCase) {
			queryString = queryString.toLowerCase();
		}


		for(Widget widget : this.nodeGraph.nodes().keySet()) {
			if(!(widget instanceof NodeWidget<?> nodeWidget)) {
				continue;
			}

			nodeWidget.setMatchesSearch(false);
			Map<ElementSearchables, Set<String>> widgetTerms = SearchIndex.widgetSearchIndex.get(nodeWidget);
			if (widgetTerms == null || widgetTerms.isEmpty()) {
				continue; // No searchable terms for this widget
			}

			Set<String> matchableTerms = widgetTerms.keySet().stream().filter(includeInSearch::contains).map(widgetTerms::get).reduce(new HashSet<>(), (a, b) -> {
				a.addAll(b);
				return a;
			});

			boolean matchesAllPart = true;
			for(String queryPart : queryString.split(" ")) {
				if(queryPart.isBlank()) {
					continue; // Skip empty parts
				}
				boolean partMatches = false;
				for(String rawTerm : matchableTerms) {
					String term = rawTerm;
					if (!searchCompareCase) {
						term = term.toLowerCase();
					}

					if (searchWithRegex) {
						if (term.matches(queryPart)) {
							partMatches = true;
							break; // Found a match, no need to check further
						}
					} else {
						if (term.contains(queryPart)) {
							partMatches = true;
							break; // Found a match, no need to check further
						}
					}
				}
				if (!partMatches) {
					matchesAllPart = false; // If any part does not match, we can stop checking
					break;
				}
			}

			nodeWidget.setMatchesSearch(matchesAllPart);
		}

	}
}
