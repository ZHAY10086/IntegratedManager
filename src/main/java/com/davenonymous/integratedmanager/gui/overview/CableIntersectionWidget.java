package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.common.IntegratedConnectionType;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.lib.gui.ColorHelper;
import com.davenonymous.integratedmanager.lib.gui.GUIHelper;
import com.davenonymous.integratedmanager.lib.gui.tooltip.LabeledLineSeparatorTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.LeftRightAlignedTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.TableTooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.List;

public class CableIntersectionWidget extends NodeWidget<Integer> {
	public ItemStack iconStack = ItemStack.EMPTY;

	public CableIntersectionWidget(Integer value) {
		super(value);
		this.setSize(6, 6);

		updateTooltip();
	}

	public void updateTooltip() {
		this.setTooltipElements();
		List<NetworkElementData> elementsOnThisNode = NetworkData.cache().elementDataByPathId.get(getValue());
		if(elementsOnThisNode != null && !elementsOnThisNode.isEmpty()) {
			var firstElement = elementsOnThisNode.getFirst();
			var position = firstElement.position;
			this.addTooltipElement(
				new LeftRightAlignedTooltipComponent(this,
					StringTooltipComponent.white(I18n.get(RegistryEntries.ITEM_CABLE.get().getDescriptionId())),
					StringTooltipComponent.orange("#" + getValue())),

				StringTooltipComponent.gray(position.toShortString())
			);
		}

		if(Minecraft.getInstance().options.advancedItemTooltips) {
			TableTooltipComponent table = new TableTooltipComponent();
			table.addRow(
				StringTooltipComponent.cyan(I18n.get("integratedmanager.message.node") + ":"),
				StringTooltipComponent.gray(String.valueOf(getValue()))
			);
			for(Integer neighborId : NetworkData.cache().paths.get(getValue()).keySet()) {
				IntegratedConnectionType connectionType = NetworkData.cache().paths.get(getValue()).get(neighborId);
				if(connectionType != null) {
					table.addRow(
						StringTooltipComponent.cyan(I18n.get("integratedmanager.message.connection") + ":"),
						StringTooltipComponent.gray(String.valueOf(neighborId)),
						StringTooltipComponent.gray(connectionType.name())
					);
				}
			}

			this.addTooltipElement(
				LabeledLineSeparatorTooltipComponent.advancedInfos(this),
				table
			);
		}
	}

	public CableIntersectionWidget setIcon(ItemStack iconStack) {
		this.iconStack = iconStack;
		return this;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, Screen screen) {
		if(!this.iconStack.isEmpty()) {
			guiGraphics.renderItem(this.iconStack, -4, -4);
		} else {
			int circleRadius = 4;
			GUIHelper.drawFilledCircle(guiGraphics, -1.5f, -1.5f, circleRadius, ColorHelper.COLOR_CYAN);
		}
		super.draw(guiGraphics, screen);
	}
}
