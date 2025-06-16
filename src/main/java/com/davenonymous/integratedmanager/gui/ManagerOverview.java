package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.gui.overview.ManagerPanel;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.lib.gui.GUI;
import com.davenonymous.integratedmanager.lib.gui.WidgetFullScreen;
import com.davenonymous.integratedmanager.lib.gui.event.GuiDataUpdatedEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetGhostSlot;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetProgressBar;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetTextBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;

public class ManagerOverview extends WidgetFullScreen {
	int sidePadding = 16;

	WidgetGhostSlot variableSlot;
	ManagerPanel managerPanel;
	WidgetProgressBar progressBar;

	public ManagerOverview() {
		super(Component.translatable("itemGroup.integratedmanager"));

		// this.setShrinkWidth(160);
	}

	protected void updateWidgetSizes() {
		sidePadding = 8;
		variableSlot.setPosition(this.width - 26 - sidePadding, 5);
		managerPanel.setDimensions(sidePadding, sidePadding + 16, this.width - (2*sidePadding), this.height - (2*sidePadding) - 16);
	}

	@Override
	protected GUI createGUI() {
		GUI gui = super.createGUI();

		var titleLabel = new WidgetTextBox(I18n.get("itemGroup.integratedmanager"));
		titleLabel.setTextColor(ChatFormatting.DARK_GRAY.getColor());
		titleLabel.setPosition(6, 6);
		titleLabel.autoWidth();
		gui.add(titleLabel);

		progressBar = new WidgetProgressBar();
		progressBar.setDisplayMode(WidgetProgressBar.EnumDisplayMode.NOTHING);
		progressBar.setPosition(6, 18);
		progressBar.setSize(titleLabel.width, 4);
		progressBar.setBorderColor(0x80000000);
		progressBar.setBackgroundColor(0x00000000);
		progressBar.setValue(66D);
		gui.add(progressBar);

		variableSlot = new WidgetGhostSlot(new ItemStack(RegistryEntries.ITEM_VARIABLE), false);
		variableSlot.setDrawTooltip(false);
		variableSlot.setEnabled(false);

		gui.add(variableSlot);

		managerPanel = new ManagerPanel();
		gui.add(managerPanel);

		gui.addListener(GuiDataUpdatedEvent.class, (event, widget) -> {
			int totalParts = NetworkData.cache().totalParts;
			if(totalParts <= 0) {
				totalParts = 1; // Avoid division by zero
			}
			progressBar.setRange(0d, (double)totalParts);
			progressBar.setValue((double)NetworkData.cache().elementDataList.size());
			progressBar.setTooltipLines(Component.literal("Elements: " + NetworkData.cache().elementDataList.size() + "/" + NetworkData.cache().totalParts));

			variableSlot.setValue(
				new ItemStack(variableSlot.getValue().getItem(), Math.max(NetworkData.cache().freeVariables, 1))
			);
			variableSlot.setTooltipElements(StringTooltipComponent.orange(
				I18n.get("integratedmanager.message.unused_variables")
			));
			return WidgetEventResult.CONTINUE_PROCESSING;
		});

		gui.addListener(AllElementsReceivedEvent.class, (event, widget) -> {
			// managerPanel.centerOnCanvas();
			var cache = NetworkData.cache();
			return WidgetEventResult.CONTINUE_PROCESSING;
		});


		updateWidgetSizes();
		return gui;
	}
}
