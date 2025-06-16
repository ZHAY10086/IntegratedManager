package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

public interface IGraphEdge {
	Widget source();
	Widget target();

	/**
	 * Returns the attraction force between the source and target widgets.
	 * A positive value indicates attraction from the source to the target,
	 * while a negative value indicates repulsion.
	 * A value of 0.0f means no attraction or repulsion.
	 *
	 * @param source The source widget of the edge.
	 * @param target The target widget of the edge.
	 * @return The attraction force between the two widgets.
	 */
	float attraction(Widget source, Widget target);

	default boolean shouldRender() {
		return true;
	}

	default int colorSource() {
		return 0x00FFFFFF;
	}

	default int colorTarget() {
		return 0xFFFFFFFF;
	}

	default void merge(IGraphEdge other) {
		// Default implementation does nothing, can be overridden by specific edge implementations
	}
}
