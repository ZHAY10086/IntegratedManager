package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;
import org.joml.Vector2f;

public class IntegrateThenApplyPositioning implements IGraphAlgorithm {

	protected IntegrateThenApplyPositioning() {
	}

	private void avoidNodesOnEdges(IGraphProvider graph) {
		for(IGraphEdge edge : graph.edges()) {
			Widget source = edge.source();
			Widget target = edge.target();
			if(source == null || target == null) {
				// If either source or target is null, skip this edge
				continue;
			}

			Vector2f sourcePositionP1 = new Vector2f(source.x + source.width / 2, source.y + source.height / 2);
			Vector2f targetPositionP2 = new Vector2f(target.x + target.width / 2, target.y + target.height / 2);
			Vector2f lineVector = new Vector2f(targetPositionP2).sub(sourcePositionP1);
			Vector2f edgeDirection = new Vector2f(lineVector).normalize();

			for(Widget node : graph.nodes().keySet()) {
				if(node == source || node == target) {
					// Skip the source and target nodes
					continue;
				}

				// d = norm(np.cross(p2-p1, p1-p3))/norm(p2-p1)
				Vector2f nodePositionP3 = new Vector2f(node.x + node.width / 2, node.y + node.height / 2);
				Vector2f p1ToP3 = new Vector2f(nodePositionP3).sub(sourcePositionP1);
				float crossProduct = Math.abs(edgeDirection.x * p1ToP3.y - edgeDirection.y * p1ToP3.x);
				float edgeLength = lineVector.length();
				if(edgeLength == 0) {
					// If the edge length is zero, skip this edge to avoid division by zero
					continue;
				}

				float distanceToEdge = crossProduct / edgeLength;
				if(distanceToEdge < 8.0f) { // Adjust this threshold as needed
					Vector2f originalVelocity = new Vector2f(graph.nodes().get(node).velocity());

					graph.setNodeVelocity(node, new Vector2f(originalVelocity).add(edgeDirection.mul(-0.0005f)));
				}
			}
		}
	}

	private void avoidCrossingEdges(IGraphProvider graph) {
		for(int iFirstEdgeNum = 0; iFirstEdgeNum < graph.edges().size(); iFirstEdgeNum++) {
			IGraphEdge firstEdge = graph.edges().get(iFirstEdgeNum);
			if(!firstEdge.shouldRender()) {
				// If the edge should not be rendered, skip it
				continue;
			}

			Widget firstSource = firstEdge.source();
			Widget firstTarget = firstEdge.target();
			if(firstSource == null || firstTarget == null) {
				// If either source or target is null, skip this edge
				continue;
			}

			for(int iSecondEdgeNum = iFirstEdgeNum + 1; iSecondEdgeNum < graph.edges().size(); iSecondEdgeNum++) {
				IGraphEdge secondEdge = graph.edges().get(iSecondEdgeNum);
				if(!secondEdge.shouldRender()) {
					// If the edge should not be rendered, skip it
					continue;
				}

				Widget secondSource = secondEdge.source();
				Widget secondTarget = secondEdge.target();
				if(secondSource == null || secondTarget == null) {
					// If either source or target is null, skip this edge
					continue;
				}

				if(firstSource == secondSource || firstSource == secondTarget ||
					firstTarget == secondSource || firstTarget == secondTarget) {
					// If the edges share a node, skip them
					continue;
				}

				// Check if the edges cross and adjust positions if necessary
				if(edgesCross(firstSource, firstTarget, secondSource, secondTarget)) {
					Vector2f firstDirection = new Vector2f(firstTarget.x - firstSource.x, firstTarget.y - firstSource.y).normalize();
					Vector2f secondDirection = new Vector2f(secondTarget.x - secondSource.x, secondTarget.y - secondSource.y).normalize();

					Vector2f firstDirectionPerpendicular = new Vector2f(firstDirection).normalize();
					Vector2f secondDirectionPerpendicular = new Vector2f(secondDirection).normalize();

					float attraction = 0.025f; // Adjust this value to control the strength of the adjustment

					var firstSourceData = graph.nodes().get(firstSource);
					var firstTargetData = graph.nodes().get(firstTarget);
					var secondTargetData = graph.nodes().get(secondTarget);
					var secondSourceData = graph.nodes().get(secondSource);

					var newFirstSourceVelocity = new Vector2f(firstSourceData.velocity())
						.add(new Vector2f(firstDirectionPerpendicular.mul(attraction)))
						.add(new Vector2f(secondDirectionPerpendicular.mul(-attraction)));
					graph.setNodeVelocity(firstSource, newFirstSourceVelocity);

					var newFirstTargetVelocity = new Vector2f(firstTargetData.velocity())
						.add(new Vector2f(firstDirectionPerpendicular.mul(-attraction)))
						.add(new Vector2f(secondDirectionPerpendicular.mul(attraction)));
					graph.setNodeVelocity(firstTarget, newFirstTargetVelocity);

					var newSecondTargetVelocity = new Vector2f(secondTargetData.velocity())
						.add(new Vector2f(firstDirectionPerpendicular.mul(-attraction)))
						.add(new Vector2f(secondDirectionPerpendicular.mul(attraction)));
					graph.setNodeVelocity(secondTarget, newSecondTargetVelocity);

					var newSecondSourceVelocity = new Vector2f(secondSourceData.velocity())
						.add(new Vector2f(firstDirectionPerpendicular.mul(attraction)))
						.add(new Vector2f(secondDirectionPerpendicular.mul(-attraction)));
					graph.setNodeVelocity(secondSource, newSecondSourceVelocity);


				}
			}
		}
	}

	private boolean ccw(Vector2f A, Vector2f B, Vector2f C) {
		return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
	}

	private boolean intersect(Vector2f A, Vector2f B, Vector2f C, Vector2f D) {
		return ccw(A, C, D) != ccw(B, C, D) && ccw(A, B, C) != ccw(A, B, D);
	}


	private boolean edgesCross(Widget firstSource, Widget firstTarget, Widget secondSource, Widget secondTarget) {
		Vector2f A = new Vector2f(firstSource.x + firstSource.width / 2, firstSource.y + firstSource.height / 2);
		Vector2f B = new Vector2f(firstTarget.x + firstTarget.width / 2, firstTarget.y + firstTarget.height / 2);
		Vector2f C = new Vector2f(secondSource.x + secondSource.width / 2, secondSource.y + secondSource.height / 2);
		Vector2f D = new Vector2f(secondTarget.x + secondTarget.width / 2, secondTarget.y + secondTarget.height / 2);

		return intersect(A, B, C, D);
	}

	private void integrateEdges(IGraphProvider graph) {
		for(IGraphEdge edge : graph.edges()) {
			Widget source = edge.source();
			Widget target = edge.target();
			if(source == null || target == null) {
				// If either source or target is null, skip this edge
				continue;
			}

			float sourceAttraction = edge.attraction(source, target);
			float targetAttraction = edge.attraction(target, source);
			Vector2f direction = new Vector2f(target.x - source.x, target.y - source.y);

			if(direction.length() < 0.01f) {
				// If the direction is too small, skip this edge to avoid division by zero
				continue;
			}

			Vector2f normalizedDirection = new Vector2f(direction).normalize();

			Vector2f sourceForces = new Vector2f(graph.nodes().get(source).velocity());
			Vector2f targetForces = new Vector2f(graph.nodes().get(target).velocity());

			Vector2f sourceVector = new Vector2f(normalizedDirection).mul(sourceAttraction * -0.0003f);
			Vector2f targetVector = new Vector2f(normalizedDirection).mul(targetAttraction * 0.0003f);

			graph.setNodeVelocity(source, sourceForces.add(sourceVector));
			graph.setNodeVelocity(target, targetForces.add(targetVector));
		}
	}

	private void integrateCanvasBorders(IGraphProvider graph) {
		float pushFactor = 0.01f; // Factor to push nodes away from the canvas edges
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
	}

	private void integrateMinDistance(IGraphProvider graph, float minDistance, float force) {
		// Integrate the minimum distance constraint between nodes
		// This is the most naive approach possible and can easily be optimized py partitioning the canvas into a grid
		for(Widget nodeA : graph.nodes().keySet()) {
			if(!nodeA.isVisible()) {
				// If the node is not visible, skip it
				continue;
			}

			Vector2f positionA = new Vector2f(graph.nodes().get(nodeA).position());
			for(Widget nodeB : graph.nodes().keySet()) {
				if(!nodeB.isVisible()) {
					// If the node is not visible, skip it
					continue;
				}

				if(nodeA == nodeB) {
					continue; // Skip self-comparison
				}
				Vector2f direction = new Vector2f(graph.nodes().get(nodeB).position()).sub(positionA);
				float distance = direction.length();
				if(distance <= 0.0001f) {
					// If the distance is zero, skip this pair to avoid division by zero
					continue;
				}
				if(distance < minDistance) {
					direction.normalize().mul((minDistance - distance) * -force);
					Vector2f velocityA = new Vector2f(graph.nodes().get(nodeA).velocity()).add(direction);
					Vector2f velocityB = new Vector2f(graph.nodes().get(nodeB).velocity()).sub(direction);
					graph.setNodeVelocity(nodeA, velocityA);
					graph.setNodeVelocity(nodeB, velocityB);
				}
			}
		}
	}

	private void applyForces(IGraphProvider graph) {
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

	private void dampenVelocities(IGraphProvider graph) {
		// Dampen the velocities of the nodes to prevent them from moving too fast
		for(Widget node : graph.nodes().keySet()) {
			Vector2f velocity = graph.nodes().get(node).velocity();
			if(velocity.length() < 0.00001f) {
				// If the velocity is negligible, we can skip damping
				continue;
			}

			// Apply damping
			velocity.mul(0.99f);
			graph.setNodeVelocity(node, velocity);
		}
	}

	@Override
	public void updatePositions(IGraphProvider graph) {
		integrateCanvasBorders(graph);
		integrateMinDistance(graph, 50.0f, 0.0005f);
		integrateEdges(graph);
		//avoidNodesOnEdges(graph);
		//avoidCrossingEdges(graph);
		applyForces(graph);
		dampenVelocities(graph);
	}
}
