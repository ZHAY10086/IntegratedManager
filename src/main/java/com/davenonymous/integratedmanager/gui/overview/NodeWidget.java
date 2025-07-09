package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.lib.gui.Icons;
import com.davenonymous.integratedmanager.lib.gui.event.*;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetPanelWithValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2f;

public class NodeWidget<T> extends WidgetPanelWithValue<T> {
	boolean initialized = false;
	int lastMouseX = -1;
	int lastMouseY = -1;

	public NodeWidget(T value) {
		super(value);

		this.addListener(
			MouseClickEvent.class, (event, widget) -> {
				if(event.button == 0 && getGUI().isShiftDown()) { // Left click
					getGUI().setDragging(true);
					this.setShouldShowTooltip(false);
					return WidgetEventResult.HANDLED;
				}
				return WidgetEventResult.CONTINUE_PROCESSING;
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

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		ManagerPanel managerPanel = getParentByType(ManagerPanel.class);
		if(managerPanel != null && managerPanel.selectedWidget != null && managerPanel.selectedWidget.equals(this)) {
			guiGraphics.fill(-5, -5, this.width + 5, this.height + 5, 0x30FFFFFF); // Draw a semi-transparent background
		}
		super.draw(guiGraphics, screen);

		if(managerPanel != null && managerPanel.selectedWidget != null && managerPanel.selectedWidget.equals(this)) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 20); // Offset for the border
			guiGraphics.blitSprite(Icons.guiIDSelectedBorder,
				-5, -5,
				this.width + 10, this.height + 10
			);
			guiGraphics.pose().popPose();
		}
	}
}
