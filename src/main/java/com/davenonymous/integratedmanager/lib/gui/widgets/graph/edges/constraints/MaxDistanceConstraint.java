package com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.constraints;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;

import java.util.Objects;

public class MaxDistanceConstraint implements IEdgeConstraint {
	private final float maxDistance;
	private final float forceFactor; // Optional: factor to scale the force applied

	public MaxDistanceConstraint(float maxDistance) {
		this(maxDistance, 1.0f);
	}

	public MaxDistanceConstraint(float maxDistance, float forceFactor) {
		this.maxDistance = maxDistance;
		this.forceFactor = forceFactor;
	}

	@Override
	public float attraction(Widget source, Widget target) {
		if(source == null || target == null) {
			return 0.0f; // No attraction if either widget is null
		}

		float dx = target.x - source.x;
		float dy = target.y - source.y;
		float distanceSquared = dx * dx + dy * dy;
		float maxDistanceSquared = maxDistance * maxDistance;

		if (distanceSquared > maxDistanceSquared) {
			// If the distance exceeds the maximum, apply a repulsion force
			return -1.0f * forceFactor * (distanceSquared - maxDistanceSquared);
		}


		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof MaxDistanceConstraint that)) {
			return false;
		}
		return Float.compare(maxDistance, that.maxDistance) == 0 && Float.compare(forceFactor, that.forceFactor) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxDistance, forceFactor);
	}
}
