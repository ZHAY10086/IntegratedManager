package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;

public class PartTargetWidget extends NodeWidget<PartData> {
	WidgetItemStack targetWidget;

	public PartTargetWidget(PartData value) {
		super(value);
		this.setSize(16, 16);

		targetWidget = new WidgetItemStack(value.targetStack);
		this.add(targetWidget);
	}
}
