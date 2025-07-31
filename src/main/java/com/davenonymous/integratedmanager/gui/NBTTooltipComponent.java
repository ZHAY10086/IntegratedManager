package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.lib.gui.tooltip.*;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class NBTTooltipComponent extends VBoxTooltipComponent {
	public NBTTooltipComponent(Tag tag) {
		super();
		this.add(buildTooltip(tag, 0));
	}

	private TooltipComponent buildTooltip(Tag tag, int depth) {
		if (tag == null) {
			return null;
		}

		if(tag.getType().isValue()) {
			return StringTooltipComponent.orange(tag.getAsString());
		}

		if (tag instanceof ListTag listTag) {
			VBoxTooltipComponent listBox = new VBoxTooltipComponent();
			listBox.setDrawBraces(true);
			listBox.setPadding(4);
			for (int i = 0; i < listTag.size(); i++) {
				Tag value = listTag.get(i);
				var nested = buildTooltip(value, depth + 1);
				if (nested == null) {
					continue;
				}
				listBox.add(nested);
			}
			if (listBox.isEmpty()) {
				return StringTooltipComponent.gray("[]");
			}
			return listBox;
		}

		if (tag instanceof CompoundTag compoundTag) {
			VBoxTooltipComponent listBox = new VBoxTooltipComponent();
			listBox.setDrawBraces(true);
			listBox.setPadding(2);
			for(var key : compoundTag.getAllKeys()) {
				Tag value = compoundTag.get(key);
				var nested = buildTooltip(value, depth + 1);
				if (nested == null) {
					continue;
				}
				listBox.add(new HBoxTooltipComponent(
					StringTooltipComponent.cyan(key + ":"),
					nested
				));
			}
			return listBox;
		}

		return null;
	}

	public static ScrollableTooltipComponent scrolling(Tag tag, int maxHeight, Widget scrollSource) {
		return new ScrollableTooltipComponent(new NBTTooltipComponent(tag), maxHeight, scrollSource);
	}
}
