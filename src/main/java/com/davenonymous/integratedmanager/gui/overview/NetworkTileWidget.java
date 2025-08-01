package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.TileData;
import com.davenonymous.integratedmanager.lib.gui.tooltip.HBoxTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetBlockInClientLevel;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public class NetworkTileWidget extends NodeWidget<NetworkElementData> {
	Widget targetWidget;

	public NetworkTileWidget(NetworkElementData networkElementData) {
		super(networkElementData);
		TileData tileData = networkElementData.tileData;

		this.setSize(16, 16);

		var level = Minecraft.getInstance().level;
		var clientDimension = level.dimension().location();
		if(clientDimension.toString().equals(tileData.level)) {
			targetWidget = new WidgetBlockInClientLevel(networkElementData.position);
		} else {
			targetWidget = new WidgetItemStack(tileData.tileStack);
		}

		if(tileData.proxyId >= 0) {
			targetWidget.addTooltipElement(
				new HBoxTooltipComponent(
					StringTooltipComponent.cyan(I18n.get("block.integrateddynamics.proxy") + ":"),
					StringTooltipComponent.green("" + tileData.proxyId)
				).setPadding(4)
			);
		}

		this.add(targetWidget);
	}
}
