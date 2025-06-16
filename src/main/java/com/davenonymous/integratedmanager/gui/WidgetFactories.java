package com.davenonymous.integratedmanager.gui;

import com.davenonymous.integratedmanager.lib.gui.tooltip.HBoxTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.StringTooltipComponent;
import com.davenonymous.integratedmanager.lib.gui.tooltip.WrappedStringTooltipComponent;

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

	}
}
