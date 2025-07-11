package com.davenonymous.integratedmanager.gui.search;


import com.davenonymous.integratedmanager.lib.gui.Icons;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetLabel;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class WidgetBadge extends WidgetLabel {
	private int activeColor;
	private int backgroundColor = 0xFFE5E5E5;
	public boolean first = false;
	public boolean last = false;
	public boolean active = false;

	public WidgetBadge(String text, int activeColor) {
		super(text);
		this.activeColor = activeColor;
		this.height = 12;
		this.autoWidth();
		this.width += 8;
	}

	@Override
	public void draw(GuiGraphics pGuiGraphics, Screen screen) {
		// background first
		int fillOffset = 0;
		int fillWidth = this.width;

		// Gray background
		pGuiGraphics.fill(0, 0, this.width, this.height, (0x80 << 24) + ChatFormatting.DARK_GRAY.getColor());

		if(active) {
			float r = (activeColor >> 16 & 0xFF) / 255.0F;
			float g = (activeColor >> 8 & 0xFF) / 255.0F;
			float b = (activeColor & 0xFF) / 255.0F;
			float alpha = (activeColor >> 24 & 0xFF) / 255.0F;
			RenderSystem.setShaderColor(r, g, b, alpha);
		}

		if(first) {
			pGuiGraphics.blit(Icons.guiDot, 0, 1, 0, 0, 5, 10, 10, 10);
			fillOffset = 5;
			fillWidth -= 5;
		}
		if(last) {
			pGuiGraphics.blit(Icons.guiDot, fillWidth - 6, 1, 5, 0, 5, 10, 10, 10);
			fillWidth -= 5;
		}

		int fillColor = backgroundColor;
		if(active) {
			fillColor = activeColor;
			RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 1);
		}

		pGuiGraphics.fill(fillOffset, 1, fillOffset + fillWidth, this.height - 1, fillColor);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		pGuiGraphics.pose().pushPose();
		pGuiGraphics.pose().translate(4, 2, 0);
		this.setTextColor(active ? 0xFFFFFF : ChatFormatting.GRAY.getColor());
		super.draw(pGuiGraphics, screen);
		pGuiGraphics.pose().popPose();
	}
}
