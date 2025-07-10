package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import com.davenonymous.integratedmanager.lib.gui.GUIHelper;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetBlockInClientLevel;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class PartTargetWidget extends NodeWidget<PartData> {
	Widget targetWidget;

	public PartTargetWidget(PartData partData) {
		super(partData);
		this.setSize(16, 16);

		var level = Minecraft.getInstance().level;
		var clientDimension = level.dimension().location();
		if(clientDimension.toString().equals(partData.level)) {
			targetWidget = new WidgetBlockInClientLevel(partData.targetPos);
		} else {
			targetWidget = new WidgetItemStack(partData.targetStack);
		}

		this.add(targetWidget);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		int circleRadius = (this.width + 4) / 2;
		GUIHelper.drawFilledCircle(guiGraphics, -1.5f, -1.5f, circleRadius, ColorHelper.COLOR_ERRORED.getRGB());
		GUIHelper.drawFilledCircle(guiGraphics, -0.5f, -0.5f, circleRadius-1, 0xFFDDDDDD);
		super.draw(guiGraphics, screen);
	}
}
