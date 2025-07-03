package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetBlockInClientLevel;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;

public class PartTargetWidget extends NodeWidget<PartData> {
	Widget targetWidget;

	public PartTargetWidget(PartData partData) {
		super(partData);
		this.setSize(16, 16);

		var level = Minecraft.getInstance().level;
		var clientDimension = level.dimension().location();
		if(clientDimension.toString().equals(partData.level)) {
			targetWidget = new WidgetBlockInClientLevel(partData.targetPos).setDrawSlot(true);
		} else {
			targetWidget = new WidgetItemStack(partData.targetStack);
		}

		this.add(targetWidget);
	}
}
