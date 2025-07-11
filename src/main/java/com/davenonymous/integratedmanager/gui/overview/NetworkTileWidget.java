package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.TileData;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetBlockInClientLevel;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;

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
				StringTooltipComponent.orange("Proxy ID: " + tileData.proxyId)
			);
		}

		this.add(targetWidget);
	}
}
