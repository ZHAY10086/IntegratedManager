package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.graph.edges.IGraphEdge;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;

public interface IGraphProvider {
	Map<Widget, NodeData> nodes();
	List<IGraphEdge> edges();

	default Vector2f getNodeVelocity(Widget widget) {
		return new Vector2f(nodes().get(widget).velocity());
	}

	default void setNodeVelocity(Widget widget, Vector2f velocity) {
		var currentData = nodes().get(widget);
		var newData = new NodeData(widget, currentData.position(), velocity);
		nodes().put(widget, newData);
	}

	default Vector2f getNodePosition(Widget widget) {
		return new Vector2f(nodes().get(widget).position());
	}

	default void setNodePosition(Widget widget, Vector2f position) {
		var currentData = nodes().get(widget);
		var newData = new NodeData(widget, position, currentData.velocity());
		nodes().put(widget, newData);
	}

	record NodeData(Widget widget, Vector2f position, Vector2f velocity) {
		public NodeData(Widget widget, Vector2f position) {
			this(widget, position, new Vector2f(0, 0));
		}

		public NodeData(Widget widget) {
			this(widget, new Vector2f(widget.x, widget.y)
				.add(
					Minecraft.getInstance().level.getRandom().nextFloat() * 0.005f,
					Minecraft.getInstance().level.getRandom().nextFloat() * 0.005f
				)
			);
		}
	}
}
