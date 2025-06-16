package com.davenonymous.integratedmanager.gui.overview;

import com.davenonymous.integratedmanager.gui.WidgetFactories;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.PartData;
import com.davenonymous.integratedmanager.lib.gui.event.MouseExitEvent;
import com.davenonymous.integratedmanager.lib.gui.event.MouseScrollEvent;
import com.davenonymous.integratedmanager.lib.gui.event.WidgetEventResult;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
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
import java.util.List;

public class NetworkPartWidget extends NodeWidget<NetworkElementData> {
	PartData part;
	ItemStack partStack;
	WidgetItemStack partWidget;
	List<ResourceLocation> usedAspects;
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

		usedAspects = new ArrayList<>();
		for(var variable : getValue().variables) {
			usedAspects.add(variable.aspect);
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

		if(Minecraft.getInstance().options.advancedItemTooltips) {
			partWidget.addTooltipElement(
				WidgetFactories.Tooltips.labelValue(I18n.get("gui.integrateddynamics.diagnostics.table.part") + ":", getValue().partId),
				WidgetFactories.Tooltips.labelValue(I18n.get("gui.integrateddynamics.diagnostics.table.position") + ":", getValue().position.toShortString())
			);
		}
	}

	protected void updateTooltips() {
		if(part.writer && !usedAspects.isEmpty()) {
			var aspect = getValue().aspects.getFirst();
			var translationKey = aspect.translationKey;
			if(I18n.exists(translationKey + ".info")) {
				partWidget.addTooltipElement(WrappedStringTooltipComponent.orange(I18n.get(translationKey + ".info")));
			} else {
				partWidget.addTooltipElement(WrappedStringTooltipComponent.orange(I18n.get(translationKey)));
			}
		} else {
			if(!getValue().aspects.isEmpty()) {
				int index = 0;
				partWidget.addTooltipElement(StringTooltipComponent.cyan(I18n.get( "info_book.integrateddynamics.tutorials.aspects") + ":"));
				for(var aspect : getValue().aspects) {
					var label = "- " + (I18n.exists(aspect.translationKey) ? I18n.get(aspect.translationKey) : aspect.uniqueName.toString());
					if(usedAspects.contains(aspect.uniqueName)) {
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

				if(selected >= 0 && selected < getValue().aspects.size()) {
					var selectedAspect = getValue().aspects.get(selected);
					partWidget.addTooltipElement(
						WrappedStringTooltipComponent.orange(I18n.get(selectedAspect.translationKey + ".info"))
					);
				} else {
					partWidget.addTooltipElement(StringTooltipComponent.orange(I18n.get("integratedmanager.message.scroll_for_aspect_details")));
				}
			}
		}
	}
}
