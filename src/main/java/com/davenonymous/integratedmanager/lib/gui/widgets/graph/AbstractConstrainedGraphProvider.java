package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractConstrainedGraphProvider implements IGraphProvider {
	private final IGraphAlgorithm algorithm = GraphAlgorithms.INTEGRATE_THEN_APPLY.get();
	private final List<IGraphEdge> edges = new ArrayList<>();
	private Map<Widget, NodeData> nodeData = new HashMap<>();

	boolean freezeActivity = false;

	public void setFreezeActivity(boolean freezeActivity) {
		this.freezeActivity = freezeActivity;
	}

	public AbstractConstrainedGraphProvider addEdge(IGraphEdge edge) {
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
}
