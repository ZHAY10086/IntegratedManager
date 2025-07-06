package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.gui.WidgetFactories;
import com.davenonymous.integratedmanager.integrated.UnknownThings;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.lib.gui.event.MouseExitEvent;
import com.davenonymous.integratedmanager.lib.gui.event.MouseScrollEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.tooltip.LabeledLineSeparatorTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.TableTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.WrappedStringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NetworkPartWidget extends NodeWidget<NetworkElementData> {
	PartData part;
	ItemStack partStack;
	WidgetItemStack partWidget;

	int selected = -1;

	public NetworkPartWidget(NetworkElementData value) {
		super(value);
		this.setSize(16, 16);

		part = value.partData;

		ResourceLocation hackedItemName = ResourceLocation.fromNamespaceAndPath(
			part.uniqueName.getNamespace(),
			"part_" + part.uniqueName.getPath()
		);

		Item partItem = BuiltInRegistries.ITEM.get(hackedItemName);
		if(partItem == null) {
			partItem = Items.BARRIER;
		}

		partStack = new ItemStack(partItem);
		partWidget = new WidgetItemStack(partStack);
		updateTooltipsInternal();
		this.add(partWidget);

		partWidget.addListener(MouseScrollEvent.class, (event, widget) -> {
			if(!partWidget.isHovered()) {
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
			if(event.down) {
				selected++;
				if(selected >= getValue().aspects.size()) {
					selected = 0;
				}
			} else {
				selected--;
				if(selected < 0) {
					selected = getValue().aspects.size() - 1;
				}
			}

			updateTooltipsInternal();

			return WidgetEventResult.CONTINUE_PROCESSING;
		});


		partWidget.addListener(
			MouseExitEvent.class, (event, widget) -> {
				selected = -1;
				updateTooltipsInternal();
				return WidgetEventResult.CONTINUE_PROCESSING;
			}
		);
	}

	private void updateTooltipsInternal() {
		partWidget.setTooltipElements(
			StringTooltipComponent.white(partStack.getHoverName().getString())
		);
		updateTooltips();


		var data = getValue();
		if(Minecraft.getInstance().options.advancedItemTooltips) {
			partWidget.addTooltipElement(
				LabeledLineSeparatorTooltipComponent.advancedInfos(partWidget),
				WidgetFactories.Tooltips.labelValue(I18n.get("gui.integrateddynamics.diagnostics.table.part") + ":", data.partId),
				WidgetFactories.Tooltips.labelValue(I18n.get("gui.integrateddynamics.diagnostics.table.position") + ":", data.position.toShortString())
			);
		}
	}

	protected void updateTooltips() {
		var data = getValue();

		boolean hasActiveAspect = part.activeAspect != null && part.activeAspect != UnknownThings.Aspect && data.getAspect(part.activeAspect) != null;
		if(part.writer && hasActiveAspect) {
			var aspect = data.getAspect(part.activeAspect);
			var translationKey = aspect.translationKey;
			if(I18n.exists(translationKey + ".info")) {
				partWidget.addTooltipElement(WrappedStringTooltipComponent.orange(I18n.get(translationKey + ".info")));
			} else {
				partWidget.addTooltipElement(WrappedStringTooltipComponent.orange(I18n.get(translationKey)));
			}
		}

		if(!part.activeAspectProperties.isEmpty()) {
			TableTooltipComponent table = new TableTooltipComponent();
			List<String> properties = part.activeAspectProperties.keySet().stream().sorted(Comparator.comparing(I18n::get)).toList();
			for(String propertyTranslationKey : properties) {
				var propertyValue = part.activeAspectProperties.get(propertyTranslationKey);
				if(propertyValue.isDefaultValue) {
					table.addRow(
						StringTooltipComponent.cyan(I18n.get(propertyTranslationKey)),
						WrappedStringTooltipComponent.gray(propertyValue.getBestName())
					);
				} else {
					table.addRow(
						StringTooltipComponent.cyan(I18n.get(propertyTranslationKey)),
						WrappedStringTooltipComponent.green(propertyValue.getBestName())
					);
				}

			}

			if(table.rows() > 0) {
				partWidget.addTooltipElement(
					new LabeledLineSeparatorTooltipComponent(partWidget, I18n.get("integratedmanager.message.properties")),
					table
				);
			}
		}

		if(!part.writer && data.channelId != -1) {
			partWidget.addTooltipElement(
				WidgetFactories.Tooltips.labelValue(I18n.get("aspect.aspecttypes.integrateddynamics.integer.channel") + ":", data.channelId)
			);
		}

		if(!(part.writer && hasActiveAspect) && !data.aspects.isEmpty()) {
			int index = 0;
			partWidget.addTooltipElement(
				new LabeledLineSeparatorTooltipComponent(partWidget, I18n.get("info_book.integrateddynamics.tutorials.aspects"))
			);
			for(var aspect : data.aspects.stream().sorted(Comparator.comparing(aspectData -> I18n.get(aspectData.translationKey))).toList()) {
				var label = "- " + (I18n.exists(aspect.translationKey) ? I18n.get(aspect.translationKey) : aspect.uniqueName.toString());
				if(part.activeAspect.equals(aspect.uniqueName)) {
					partWidget.addTooltipElement(StringTooltipComponent.green(label));
				} else {
					if(index == selected) {
						partWidget.addTooltipElement(StringTooltipComponent.green(label));
					} else {
						partWidget.addTooltipElement(StringTooltipComponent.gray(label));
					}
				}

				index++;
			}

			if(selected >= 0 && selected < data.aspects.size()) {
				var selectedAspect = data.aspects.get(selected);
				partWidget.addTooltipElement(
					WrappedStringTooltipComponent.orange(I18n.get(selectedAspect.translationKey + ".info"))
				);
			} else {
				partWidget.addTooltipElement(StringTooltipComponent.orange(I18n.get("integratedmanager.message.scroll_for_aspect_details")));
			}
		}

	}
}
