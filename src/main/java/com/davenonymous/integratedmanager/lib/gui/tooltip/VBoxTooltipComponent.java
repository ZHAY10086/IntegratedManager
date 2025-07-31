package com.davenonymous.integratedmanager.lib.gui.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VBoxTooltipComponent implements TooltipComponent, ClientTooltipComponent {
	private final List<TooltipComponent> components = new ArrayList<>();
	private int padding = 0;
	private boolean drawBraces = false;

	private BoxAlignment alignment = BoxAlignment.START;

	public VBoxTooltipComponent(TooltipComponent... components) {
		this.components.addAll(Arrays.asList(components));
	}

	public VBoxTooltipComponent add(TooltipComponent... component) {
		this.components.addAll(Arrays.asList(component));
		return this;
	}

	public VBoxTooltipComponent setDrawBraces(boolean drawBraces) {
		this.drawBraces = drawBraces;
		return this;
	}

	public VBoxTooltipComponent setAlignment(BoxAlignment alignment) {
		this.alignment = alignment;
		return this;
	}

	public VBoxTooltipComponent setPadding(int padding) {
		this.padding = padding;
		return this;
	}

	public int count() {
		return components.size();
	}

	public boolean isEmpty() {
		return components.isEmpty();
	}


	public VBoxTooltipComponent clear() {
		components.clear();
		return this;
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		int currentY = y;
		int maxWidth = getWidth(font);
		if(drawBraces) {
			currentY += 3; // Offset for the left brace
		}

		for(TooltipComponent component : components) {
			if(component instanceof ClientTooltipComponent clientComponent) {
				int xOffset = x;
				if(drawBraces) {
					xOffset += 3; // Offset for the left brace
				}
				int componentHeight = clientComponent.getWidth(font);
				if(alignment == BoxAlignment.CENTER) {
					xOffset += (maxWidth - componentHeight) / 2;
				} else if(alignment == BoxAlignment.END) {
					xOffset += maxWidth - componentHeight;
				}

				clientComponent.renderImage(font, xOffset, currentY, guiGraphics);
				currentY += clientComponent.getHeight() + padding;
			}
		}

		if(drawBraces) {
			int xBrace = x;
			guiGraphics.fill(xBrace, y, xBrace + 4, y+1, 0x80FFFFFF); // Left brace
			guiGraphics.fill(xBrace-1, y+1, xBrace, currentY-1, 0x80FFFFFF); // Left brace
			guiGraphics.fill(xBrace, currentY-1, xBrace + 4, currentY, 0x80FFFFFF); // Bottom left brace
		}
	}

	@Override
	public int getHeight() {
		int sum = 0;
		for(TooltipComponent component : components) {
			if(component instanceof ClientTooltipComponent clientComponent) {
				sum += clientComponent.getHeight() + padding;
			}
		}
		if(drawBraces) {
			sum += 6; // 3 pixels for the top brace and 3 pixels for the bottom brace
		}
		return sum;
	}

	@Override
	public int getWidth(Font font) {
		int max = 0;
		for(TooltipComponent component : components) {
			if(component instanceof ClientTooltipComponent clientComponent) {
				max = Math.max(max, clientComponent.getWidth(font));
			}
		}
		return max;
	}
}
