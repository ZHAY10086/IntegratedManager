package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

import java.util.Objects;

public class MinDistanceConstraint implements IEdgeConstraint {
	private final float minDistance;
	private final float forceFactor; // Optional: factor to scale the force applied

	public MinDistanceConstraint(float minDistance) {
		this(minDistance, 1.0f);
	}

	public MinDistanceConstraint(float minDistance, float forceFactor) {
		this.minDistance = minDistance;
		this.forceFactor = forceFactor;
	}

	@Override
	public float attraction(Widget source, Widget target) {
		float dx = target.x - source.x;
		float dy = target.y - source.y;
		float distanceSquared = dx * dx + dy * dy;
		float minDistanceSquared = minDistance * minDistance;

		if (distanceSquared < minDistanceSquared) {
			// If the distance exceeds the maximum, apply a repulsion force
			return -1.0f * forceFactor * (distanceSquared - minDistanceSquared);
		}

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof MinDistanceConstraint that)) {
			return false;
		}
		return Float.compare(minDistance, that.minDistance) == 0 && Float.compare(forceFactor, that.forceFactor) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(minDistance, forceFactor);
	}
}
