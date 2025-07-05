package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints.IEdgeConstraint;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints.MaxDistanceConstraint;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints.MinDistanceConstraint;

import java.util.HashMap;
import java.util.Map;

public class ConstrainedGraphEdge extends AbstractGraphEdge {
	Map<IEdgeConstraint, Float> constraints = new HashMap<>();
	boolean shouldRender = true;

	public ConstrainedGraphEdge(Widget source, Widget target) {
		super(source, target);
	}

	/**
	 * Adds a constraint to this edge.
	 *
	 * @param constraint The constraint to add.
	 * @param weight      The weight of the constraint.
	 */
	public ConstrainedGraphEdge addConstraint(IEdgeConstraint constraint, float weight) {
		constraints.put(constraint, weight);
		return this;
	}

	public ConstrainedGraphEdge addConstraint(IEdgeConstraint constraint) {
		constraints.put(constraint, 1.0f);
		return this;
	}

	@Override
	public void merge(IGraphEdge other) {
		if(other instanceof ConstrainedGraphEdge otherConstrainedEdge) {
			// Merge constraints from the other edge
			for (Map.Entry<IEdgeConstraint, Float> entry : otherConstrainedEdge.constraints.entrySet()) {
				IEdgeConstraint constraint = entry.getKey();
				float weight = entry.getValue();

				// If the constraint already exists, sum the weights
				constraints.merge(constraint, weight, Float::min);
			}

			this.shouldRender |= otherConstrainedEdge.shouldRender;
			this.setColorTarget(otherConstrainedEdge.colorTarget());
			this.setColorSource(otherConstrainedEdge.colorSource());
			if(this.style == null && otherConstrainedEdge.getStyle() != null) {
				this.setStyle(otherConstrainedEdge.getStyle());
			}
		}
	}

	@Override
	public float attraction(Widget source, Widget target) {
		float attraction = 0.0f;
		for (Map.Entry<IEdgeConstraint, Float> entry : constraints.entrySet()) {
			IEdgeConstraint constraint = entry.getKey();
			float weight = entry.getValue();

			float sourceAttraction = constraint.attraction(source, target);
			float targetAttraction = constraint.attraction(target, source);

			attraction += (sourceAttraction + targetAttraction) * weight;
		}
		return attraction;
	}

	@Override
	public boolean shouldRender() {
		return shouldRender;
	}

	public ConstrainedGraphEdge setShouldRender(boolean shouldRender) {
		this.shouldRender = shouldRender;
		return this;
	}

	public static ConstrainedGraphEdge createMinDistanceEdge(Widget source, Widget target, float distance) {
		return new ConstrainedGraphEdge(source, target)
			.setShouldRender(false)
			.addConstraint(new MinDistanceConstraint(distance, 0.010f));
	}

	public static ConstrainedGraphEdge createMaxDistanceEdge(Widget source, Widget target, float distance) {
		return new ConstrainedGraphEdge(source, target)
			.setShouldRender(false)
			.addConstraint(new MaxDistanceConstraint(distance, 0.005f));
	}

	public static ConstrainedGraphEdge createConstrainedEdge(Widget source, Widget target) {
		return createConstrainedEdge(source, target, 60.0f);
	}

	public static ConstrainedGraphEdge createConstrainedEdge(Widget source, Widget target, float distance) {
		return new ConstrainedGraphEdge(source, target)
			.addConstraint(new MaxDistanceConstraint(distance, 0.005f))
			.addConstraint(new MinDistanceConstraint(distance, 0.005f));
	}
}
