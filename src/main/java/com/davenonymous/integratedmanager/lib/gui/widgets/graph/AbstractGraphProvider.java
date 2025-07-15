package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetPanel;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.LineStyle;

import java.util.*;

public class AbstractGraphProvider extends WidgetPanel implements IGraphProvider {
	private final IGraphAlgorithm algorithm;
	private List<IGraphEdge> edges = new ArrayList<>();
	private Map<Widget, NodeData> nodeData = new HashMap<>();
	private boolean freezeActivity = false;
	private Map<Widget, List<Widget>> nodeDescendants = new HashMap<>();
	private Map<Widget, List<Widget>> nodeAncestors = new HashMap<>();

	public AbstractGraphProvider(IGraphAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public void runTick() {
		this.algorithm.updatePositions(this);
	}

	@Override
	public void add(Widget widget) {
		super.add(widget);
		nodeData.put(widget, new NodeData(widget));
	}

	@Override
	public void remove(Widget widget) {
		super.remove(widget);
	}

	@Override
	public void clear() {
		super.clear();
		this.nodeData.clear();
		this.edges.clear();
		this.nodeDescendants.clear();
		this.nodeAncestors.clear();
	}

	@Override
	public Map<Widget, NodeData> nodes() {
		return this.nodeData;
	}

	@Override
	public List<IGraphEdge> edges() {
		return this.edges;
	}

	public List<IGraphEdge> edges(Widget node) {
		return this.edges.stream().filter(edge -> edge.source() == node || edge.target() == node).toList();
	}

	public AbstractGraphProvider addEdge(IGraphEdge edge) {
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

		if(edge.shouldRender() && edge.getStyle() == LineStyle.ARROW) {
			this.nodeDescendants.computeIfAbsent(edge.source(), k -> new ArrayList<>()).add(edge.target());
			this.nodeAncestors.computeIfAbsent(edge.target(), k -> new ArrayList<>()).add(edge.source());
		}
		this.edges.add(edge);
		return this;
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
				if(velocity > 0.035f) {
					// If any node has a velocity greater than a small threshold, we consider the graph not settled
					iterations++;
					continue SETTLE;
				}
			}
			break;
		} while (iterations < maxIterations);

		if (iterations >= maxIterations) {
			IntegratedManager.LOGGER.debug("Node graph did not settle after {} iterations", maxIterations);
		} else {
			IntegratedManager.LOGGER.debug("Node graph settled after {} iterations", iterations);
		}
	}

	public boolean isFrozen() {
		return this.freezeActivity;
	}

	public void setFreezeActivity(boolean freezeActivity) {
		this.freezeActivity = freezeActivity;
	}


	public List<Widget> getDescendants(Widget widget) {
		List<Widget> descendants = new ArrayList<>();
		Queue<Widget> queue = new LinkedList<>();
		queue.add(widget);
		while(!queue.isEmpty()) {
			Widget current = queue.poll();
			if(!nodeDescendants.containsKey(current)) {
				continue; // No children for this node
			}

			for(Widget child : nodeDescendants.get(current)) {
				if(!descendants.contains(child)) {
					descendants.add(child);
					queue.add(child);
				}
			}
		}

		return descendants;
	}

	public List<Widget> getAncestors(Widget widget) {
		List<Widget> ancestors = new ArrayList<>();
		Queue<Widget> queue = new LinkedList<>();
		queue.add(widget);
		while(!queue.isEmpty()) {
			Widget current = queue.poll();
			if(!nodeAncestors.containsKey(current)) {
				continue; // No parents for this node
			}

			for(Widget parent : nodeAncestors.get(current)) {
				if(!ancestors.contains(parent)) {
					ancestors.add(parent);
					queue.add(parent);
				}
			}
		}

		return ancestors;
	}

}
