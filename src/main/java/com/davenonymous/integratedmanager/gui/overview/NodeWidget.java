package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.lib.gui.event.*;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetPanelWithValue;
import org.joml.Vector2f;

public class NodeWidget<T> extends WidgetPanelWithValue<T> {
	boolean initialized = false;
	int lastMouseX = -1;
	int lastMouseY = -1;

	public NodeWidget(T value) {
		super(value);

		this.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(event.button == 0) { // Left click
					getGUI().setDragging(true);
					this.setShouldShowTooltip(false);
				}
				return WidgetEventResult.HANDLED;
			});

		this.addListener(
			UpdateScreenEvent.class, (eventIgnore, widgetIgnore) -> {
				if(initialized) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}
				initialized = true;

				getGUI().addListener(
					MouseReleasedEvent.class, (event, widget) -> {
						if(event.button == 0) { // Left click
							getGUI().setDragging(false);
							this.setShouldShowTooltip(true);
						}
						return WidgetEventResult.CONTINUE_PROCESSING;
					});

				return WidgetEventResult.CONTINUE_PROCESSING;
			});


		this.addListener(
			MouseMoveEvent.class, (event, widget) -> {
				if(!getGUI().isDragging() || !isHovered()) {
					lastMouseX = -1; // Reset last mouse position
					lastMouseY = -1;
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				WidgetNodeGraph nodeGraph = getParentByType(WidgetNodeGraph.class);
				if(nodeGraph == null) {
					return WidgetEventResult.CONTINUE_PROCESSING;
				}

				if(lastMouseX == -1 || lastMouseY == -1) {
					lastMouseX = event.x;
					lastMouseY = event.y;
					return WidgetEventResult.CONTINUE_PROCESSING; // Skip first move event
				}

				int deltaX = event.x - lastMouseX;
				int deltaY = event.y - lastMouseY;
				lastMouseX = event.x;
				lastMouseY = event.y;

				Vector2f direction = new Vector2f(deltaX, deltaY);
				nodeGraph.setNodeVelocity(this, new Vector2f(direction).mul(1f));

				return WidgetEventResult.HANDLED;
			});
	}
}
