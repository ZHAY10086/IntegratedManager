package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;
import org.joml.Vector2f;

public class IntegrateThenApplyPositioning implements IGraphAlgorithm {

	protected IntegrateThenApplyPositioning() {
	}

	@Override
	public void updatePositions(IGraphProvider graph) {
		for(IGraphEdge edge : graph.edges()) {
			Widget source = edge.source();
			Widget target = edge.target();
			if(source == null || target == null) {
				// If either source or target is null, skip this edge
				continue;
			}

			float sourceAttraction = edge.attraction(source, target);
			float targetAttraction = edge.attraction(target, source);
			Vector2f direction = new Vector2f(target.x - source.x, target.y - source.y).normalize();
			if(direction.length() < 0.01f) {
				// If the direction is too small, skip this edge to avoid division by zero
				continue;
			}

			Vector2f sourceForces = new Vector2f(graph.nodes().get(source).velocity()).mul(0.994f);
			Vector2f targetForces = new Vector2f(graph.nodes().get(target).velocity()).mul(0.994f);

			Vector2f sourceVector = new Vector2f(direction).mul(sourceAttraction * -0.0003f);
			Vector2f targetVector = new Vector2f(direction).mul(targetAttraction * 0.0003f);

			graph.setNodeVelocity(source, sourceForces.add(sourceVector));
			graph.setNodeVelocity(target, targetForces.add(targetVector));
		}

		float pushFactor = 0.01f; // Factor to push nodes away from each other
		for(Widget node : graph.nodes().keySet()) {
			if(node.x <= 64) {
				// If the node is too close to the left edge, push it away
				Vector2f velocity = graph.nodes().get(node).velocity();
				velocity.x += pushFactor; // Push it right
				graph.setNodeVelocity(node, velocity);
			}
			if(node.y <= 64) {
				// If the node is too close to the top edge, push it down
				Vector2f velocity = graph.nodes().get(node).velocity();
				velocity.y += pushFactor; // Push it down
				graph.setNodeVelocity(node, velocity);
			}
			if(node.x >= 1024 - 64) {
				// If the node is too close to the right edge, push it away
				Vector2f velocity = graph.nodes().get(node).velocity();
				velocity.x -= pushFactor; // Push it left
				graph.setNodeVelocity(node, velocity);
			}
			if(node.y >= 1024 - 64) {
				// If the node is too close to the bottom edge, push it up
				Vector2f velocity = graph.nodes().get(node).velocity();
				velocity.y -= pushFactor; // Push it up
				graph.setNodeVelocity(node, velocity);
			}
		}

		// Apply the calculated forces to the nodes
		for(Widget node : graph.nodes().keySet()) {
			IGraphProvider.NodeData nodeData = graph.nodes().get(node);
			Vector2f velocity = nodeData.velocity();
			if(velocity.length() < 0.025f) {
				// If the velocity is negligible, we can skip updating the position
				continue;
			}

			Vector2f position = new Vector2f(nodeData.position());
			position.add(velocity);
			graph.setNodePosition(node, position);

			// Update the node's position in the graph
			node.setPosition(Math.round(position.x), Math.round(position.y));
		}

	}
}
