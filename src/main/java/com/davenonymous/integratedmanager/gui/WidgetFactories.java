package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.lib.gui.tooltip.HBoxTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.LeftRightAlignedTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.WrappedStringTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

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

		public static TooltipComponent[] variableHeader(VariableData variable, boolean labelOnly) {
			List<TooltipComponent> tooltipElements = new ArrayList<>();
			boolean hasDescription = I18n.exists(variable.translationKey + ".info");
			boolean hasName = I18n.exists(variable.translationKey);
			boolean hasValue = variable.valueData != null;
			boolean isValueType = variable.facadeClassName.equals("ValueTypeVariableFacade");

			if(!variable.label.isBlank()) {
				var labelTooltip = WrappedStringTooltipComponent.yellow(variable.label);
				tooltipElements.add(labelTooltip);
			}

			if(hasName && hasValue && isValueType) {
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
			}

			return tooltipElements.toArray(new TooltipComponent[0]);
		}

	}
}
