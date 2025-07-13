package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.gui.WidgetFactories;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.common.TypeData;
import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.lib.gui.tooltip.*;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VariableFacadeWidget extends NodeWidget<VariableData> {
	WidgetItemStack variableWidget;
	String searchHaystack = "";

	public VariableFacadeWidget(VariableData variable) {
		super(variable);
		this.setSize(16, 16);

		List<String> stringsForSearch = new ArrayList<>();
		stringsForSearch.add(I18n.get(variable.translationKey));

		variableWidget = new WidgetItemStack(variable.variableStack.copy());
		variableWidget.setDrawTooltip(false);
		TooltipComponent[] tooltipComponents = WidgetFactories.Tooltips.variableHeader(variable, variableWidget);
		if(tooltipComponents.length > 0 && variable.id != -1 && tooltipComponents[0] instanceof ClientTooltipComponent firstRow) {
			tooltipComponents[0] = new LeftRightAlignedTooltipComponent(variableWidget, firstRow, StringTooltipComponent.cyan("#" + variable.id));
		}
		variableWidget.setTooltipElements(tooltipComponents);

		boolean isValueType = variable.facadeClassName.equals("ValueTypeVariableFacade");
		if(!isValueType) {
			TableTooltipComponent table = new TableTooltipComponent();

			if(variable.inputTypes != null && !variable.inputTypes.isEmpty()) {
				int i = 1;
				for(TypeData inputType : variable.inputTypes) {
					String inputTypeName = I18n.exists(inputType.typeTranslationKey) ? I18n.get(inputType.typeTranslationKey) : inputType.valueType.toString();
					String label = variable.inputTypes.size() > 1 ? "Input " + i + ":" : "Input:";
					boolean isAnyType = inputType.typeTranslationKey.equals("valuetype.integrateddynamics.any");

					if(variable.referencedVariableIds.size() > i - 1) {
						int referencedVariableId = variable.referencedVariableIds.get(i - 1);
						VariableData referencedVariable = NetworkData.cache().variableDataById.get(referencedVariableId);
						if(referencedVariable != null) {

							if(referencedVariable.valueData != null) {

								ValueData value = referencedVariable.valueData;
								String translatedType = I18n.exists(value.typeTranslationKey) ? I18n.get(value.typeTranslationKey) : value.valueType.toString();
								String translatedValue = I18n.exists(value.valueTranslationKey) ? I18n.get(value.valueTranslationKey) : value.stringValue;

								table.addRow(
									StringTooltipComponent.cyan(label),
									StringTooltipComponent.orange(translatedType + (isAnyType ? "*" : "")),
									WrappedStringTooltipComponent.green(translatedValue)

								);
							} else {
								table.addRow(
									StringTooltipComponent.cyan(label),
									StringTooltipComponent.orange(inputTypeName)
								);
							}

						}
					} else {
						table.addRow(
							StringTooltipComponent.cyan(label),
							StringTooltipComponent.orange(inputTypeName)
						);
					}
					i++;
				}
			}

			if(variable.outputType != null) {
				String outputTypeName = I18n.exists(variable.outputType.typeTranslationKey)
										? I18n.get(variable.outputType.typeTranslationKey)
										: variable.outputType.valueType.toString();
				boolean isAnyType = variable.outputType.typeTranslationKey.equals("valuetype.integrateddynamics.any");
				if(variable.valueData != null) {
					ValueData value = variable.valueData;
					String translatedType = I18n.exists(value.typeTranslationKey) ? I18n.get(value.typeTranslationKey) : value.valueType.toString();
					String translatedValue = I18n.exists(value.valueTranslationKey) ? I18n.get(value.valueTranslationKey) : value.stringValue;

					table.addRow(
						StringTooltipComponent.cyan("Output:"),
						StringTooltipComponent.orange(translatedType + (isAnyType ? "*" : "")),
						WrappedStringTooltipComponent.green(translatedValue)
					);
				} else {
					table.addRow(
						StringTooltipComponent.cyan("Output:"),
						StringTooltipComponent.orange(outputTypeName)
					);
				}
			} else if(variable.valueData != null) {
				ValueData value = variable.valueData;
				String translatedType = I18n.exists(value.typeTranslationKey) ? I18n.get(value.typeTranslationKey) : value.valueType.toString();
				String translatedValue = I18n.exists(value.valueTranslationKey) ? I18n.get(value.valueTranslationKey) : value.stringValue;

				table.addRow(
					StringTooltipComponent.cyan("Output:"),
					StringTooltipComponent.orange(translatedType),
					WrappedStringTooltipComponent.green(translatedValue)
				);
			}

			if(table.rows() > 0) {
				variableWidget.addTooltipElement(
					new LabeledLineSeparatorTooltipComponent(variableWidget, "IO"),
					table
				);
			}


			TableTooltipComponent tableAspectProperties = new TableTooltipComponent();
			List<String> properties = variable.aspectProperties.keySet().stream().sorted(Comparator.comparing(I18n::get)).toList();
			for(String propertyTranslationKey : properties) {
				var propertyValue = variable.aspectProperties.get(propertyTranslationKey);
				if(propertyValue.isDefaultValue) {
					tableAspectProperties.addRow(
						StringTooltipComponent.cyan(I18n.get(propertyTranslationKey)),
						WrappedStringTooltipComponent.gray(propertyValue.getBestName())
					);
				} else {
					tableAspectProperties.addRow(
						StringTooltipComponent.cyan(I18n.get(propertyTranslationKey)),
						WrappedStringTooltipComponent.green(propertyValue.getBestName())
					);
				}

			}

			if(tableAspectProperties.rows() > 0) {
				variableWidget.addTooltipElement(
					new LabeledLineSeparatorTooltipComponent(variableWidget, I18n.get("integratedmanager.message.properties")),
					tableAspectProperties
				);
			}
		}

		TableTooltipComponent outputOperatorTable = new TableTooltipComponent();
		if(variable.valueData != null && variable.valueData.typeTranslationKey.equals("valuetype.integrateddynamics.operator")) {
			ValueData operatorValue = variable.valueData;

			if(operatorValue.inputTypes != null && !operatorValue.inputTypes.isEmpty()) {
				int i = 1;
				for(TypeData inputType : operatorValue.inputTypes) {
					String inputTypeName = I18n.exists(inputType.typeTranslationKey) ? I18n.get(inputType.typeTranslationKey) : inputType.valueType.toString();
					String label = operatorValue.inputTypes.size() > 1 ? "Input " + i + ":" : "Input:";

					outputOperatorTable.addRow(
						StringTooltipComponent.cyan(label),
						StringTooltipComponent.orange(inputTypeName)
					);

					i++;
				}
			}

			if(operatorValue.outputType != null) {
				String outputTypeName = I18n.exists(operatorValue.outputType.typeTranslationKey)
										? I18n.get(operatorValue.outputType.typeTranslationKey)
										: operatorValue.outputType.valueType.toString();
				outputOperatorTable.addRow(
					StringTooltipComponent.cyan("Output:"),
					StringTooltipComponent.orange(outputTypeName)
				);
			}

			if(outputOperatorTable.rows() > 0) {
				variableWidget.addTooltipElement(
					new LabeledLineSeparatorTooltipComponent(variableWidget, "Output Operator"),
					outputOperatorTable
				);
			}
		}

		if(Minecraft.getInstance().options.advancedItemTooltips) {
			TableTooltipComponent table = new TableTooltipComponent();
			table.addRow(
				StringTooltipComponent.cyan(I18n.get("integratedmanager.message.class_name") + ":"),
				WrappedStringTooltipComponent.gray(variable.facadeClassName)
			);
			if(variable.aspect != null) {
				table.addRow(
					StringTooltipComponent.cyan(I18n.get("aspect.integrateddynamics.name") + ":"),
					WrappedStringTooltipComponent.gray(variable.aspect.toString())
				);
			}
			if(variable.type != null) {
				table.addRow(
					StringTooltipComponent.cyan(I18n.get("valuetype.integrateddynamics.value_type") + ":"),
					WrappedStringTooltipComponent.gray(variable.type.toString())
				);
			}

			variableWidget.addTooltipElement(
				LabeledLineSeparatorTooltipComponent.advancedInfos(variableWidget),
				table
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
