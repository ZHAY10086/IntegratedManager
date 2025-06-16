package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

@FunctionalInterface
public interface IEdgeConstraint {
	float attraction(Widget source, Widget target);
}
