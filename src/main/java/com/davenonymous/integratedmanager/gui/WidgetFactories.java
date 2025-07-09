package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.lib.gui.Icons;
import com.davenonymous.integratedmanager.lib.gui.tooltip.*;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class WidgetFactories {

	public static class Tooltips {
		public static HBoxTooltipComponent labelValue(String label, Object value) {
			return new HBoxTooltipComponent(
				StringTooltipComponent.cyan(label),
				WrappedStringTooltipComponent.gray(value.toString())
			);
		}

		public static HBoxTooltipComponent headingValue(String label, Object value) {
			return new HBoxTooltipComponent(
				StringTooltipComponent.white(label),
				WrappedStringTooltipComponent.green(value.toString())
			);
		}

		public static TooltipComponent[] variableHeader(VariableData variable, Widget parent) {
			List<TooltipComponent> tooltipElements = new ArrayList<>();
			boolean hasDescription = I18n.exists(variable.translationKey + ".info");
			boolean hasName = I18n.exists(variable.translationKey);
			boolean hasValue = variable.valueData != null;
			boolean isValueType = variable.facadeClassName.equals("ValueTypeVariableFacade");

			if(!variable.label.isBlank()) {
				var labelTooltip = WrappedStringTooltipComponent.yellow(variable.label);
				tooltipElements.add(labelTooltip);
			}

			if(hasName && hasValue && isValueType && variable.valueData.itemStackValues.size() <= 1) {
				ValueData value = variable.valueData;
				String translatedValue = I18n.exists(value.valueTranslationKey) ? I18n.get(value.valueTranslationKey) : value.stringValue;
				tooltipElements.add(headingValue(I18n.get(variable.translationKey) + ":", translatedValue));
			} else if(hasName) {
				tooltipElements.add(WrappedStringTooltipComponent.white(I18n.get(variable.translationKey)));
			}

			if(hasDescription) {
				tooltipElements.add(WrappedStringTooltipComponent.orange(I18n.get(variable.translationKey + ".info")));
			}

			if(isValueType && hasValue) {
				ValueData value = variable.valueData;

				String infoTranslationKey = value.valueTranslationKey + ".info";
				if(I18n.exists(infoTranslationKey)) {
					tooltipElements.add(WrappedStringTooltipComponent.orange(I18n.get(infoTranslationKey)));
				}

				VBoxTooltipComponent valueBox = new VBoxTooltipComponent();
				if(!variable.valueData.itemStackValues.isEmpty()) {
					IngredientBoxTooltipComponent ingredientBox = new IngredientBoxTooltipComponent(
						variable.valueData.itemStackValues.stream().map(ItemStack::getItem).toList()
					);
					valueBox.add(ingredientBox);
				}

				TableTooltipComponent fluidBox = new TableTooltipComponent();
				if(!variable.valueData.fluidStackValues.isEmpty()) {
					for (var fluidStack : variable.valueData.fluidStackValues) {
						String fluidName = I18n.exists(fluidStack.getDescriptionId()) ? I18n.get(fluidStack.getDescriptionId()) : fluidStack.getFluid().toString();
						fluidBox.addRow(
							new ItemStackTooltipComponent(fluidStack.getFluidType().getBucket(fluidStack)).setShowLabel(false),
							StringTooltipComponent.white(fluidName),
							StringTooltipComponent.gray(fluidStack.getAmount() + " mB")
						);
					}
				}
				if(variable.valueData.forgeEnergyValue != 0) {
					fluidBox.addRow(
						new ItemStackTooltipComponent(new ItemStack(Items.REDSTONE)).setShowLabel(false),
						StringTooltipComponent.white(I18n.get("general.integrateddynamics.energy")),
						StringTooltipComponent.gray(variable.valueData.forgeEnergyValue + " " + I18n.get("general.integrateddynamics.energy_unit"))
					);
				}

				if(fluidBox.rows() > 0) {
					valueBox.add(fluidBox);
				}

				if(!valueBox.isEmpty()) {
					BackgroundTooltipComponent itemBox = new BackgroundTooltipComponent(
						valueBox,
						Icons.guiIDVariableBackground
					);
					tooltipElements.add(new CenteredTooltipComponent(parent, itemBox));
				}
			}

			return tooltipElements.toArray(new TooltipComponent[0]);
		}

	}
}
