package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.gui.WidgetFactories;
import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.lib.gui.tooltip.HBoxTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Set;

public class VariableFacadeWidget extends NodeWidget<VariableData> {
	WidgetItemStack variableWidget;

	public VariableFacadeWidget(VariableData variable) {
		super(variable);
		this.setSize(16, 16);

		variableWidget = new WidgetItemStack(variable.variableStack.copy());
		variableWidget.setDrawTooltip(false);
		boolean hasDescription = I18n.exists(variable.translationKey + ".info");
		boolean hasName = I18n.exists(variable.translationKey);

		String heading;
		if(hasName && hasDescription) {
			heading = I18n.get(variable.translationKey) + ":";
		} else if(hasName) {
			heading = I18n.get(variable.translationKey) + ":";
		} else if(hasDescription) {
			heading = I18n.get(variable.translationKey + ".info");
		} else {
			heading = I18n.get("integratedmanager.message.unknown_variable_type", variable.type);
		}

		if(!variable.label.isBlank()) {
			variableWidget.setTooltipElements(
				WrappedStringTooltipComponent.yellow(variable.label)
			);
		} else {
			variableWidget.setTooltipElements();
		}

		Set<ResourceLocation> labelOperators = Set.of(
			ValueTypes.OPERATOR.getUniqueName(),
			ValueTypes.BOOLEAN.getUniqueName(),
			ValueTypes.INTEGER.getUniqueName(),
			ValueTypes.STRING.getUniqueName(),
			ValueTypes.DOUBLE.getUniqueName(),
			ValueTypes.LONG.getUniqueName()
		);
		if(variable.aspect != null && variable.valueData != null) {
			ValueData value = variable.valueData;
			ResourceLocation varType = variable.aspect;
			String translatedValue = I18n.exists(value.valueTranslationKey) ? I18n.get(value.valueTranslationKey) : value.stringValue;
			String translatedType = I18n.exists(value.typeTranslationKey) ? I18n.get(value.typeTranslationKey) : value.valueType.toString();

			boolean showTranslatedValue = labelOperators.contains(varType) || (hasName && hasDescription);
			if(showTranslatedValue) {
				variableWidget.addTooltipElement(
					WidgetFactories.Tooltips.headingValue(heading, translatedValue)
				);
			} else {
				variableWidget.addTooltipElement(
					StringTooltipComponent.white(heading)
				);
			}

			if(hasName && hasDescription) {
				WrappedStringTooltipComponent valueTooltip = WrappedStringTooltipComponent.orange(I18n.get(variable.translationKey + ".info"));
				variableWidget.addTooltipElement(valueTooltip);
			}

			variableWidget.addTooltipElement(
				WidgetFactories.Tooltips.labelValue("Type:", translatedType)
			);
		}

		if(Minecraft.getInstance().options.advancedItemTooltips) {
			variableWidget.addTooltipElement(
				WidgetFactories.Tooltips.labelValue("Variable ID:", variable.id),
				WidgetFactories.Tooltips.labelValue(I18n.get("aspect.integrateddynamics.name") + ":", variable.aspect),
				WidgetFactories.Tooltips.labelValue(I18n.get("valuetype.integrateddynamics.value_type") + ":", variable.type),
				WidgetFactories.Tooltips.labelValue(I18n.get("aspect.integrateddynamics.read.any.network.value") + ":", variable.valueData.stringValue)
			);
		}

		this.add(variableWidget);
	}

	@Override
	public void setShouldShowTooltip(boolean shouldShowTooltip) {
		super.setShouldShowTooltip(shouldShowTooltip);
		variableWidget.setShouldShowTooltip(shouldShowTooltip);
	}
}
