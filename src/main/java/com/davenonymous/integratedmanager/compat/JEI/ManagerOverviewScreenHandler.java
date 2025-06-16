package com.davenonymous.integratedmanager.compat.JEI;

import com.davenonymous.integratedmanager.gui.ManagerOverview;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManagerOverviewScreenHandler implements IScreenHandler<ManagerOverview> {
	@Override
	public @Nullable IGuiProperties apply(ManagerOverview guiScreen) {
		return new GuiProperties(guiScreen);
	}

	record GuiProperties(ManagerOverview guiScreen) implements IGuiProperties {

		@Override
		public @NotNull Class<? extends Screen> screenClass() {
			return guiScreen.getClass();
		}

		@Override
		public int guiLeft() {
			return 0;
		}

		@Override
		public int guiTop() {
			return 0;
		}

		@Override
		public int guiXSize() {
			return guiScreen.width;
		}

		@Override
		public int guiYSize() {
			return guiScreen.height;
		}

		@Override
		public int screenWidth() {
			return guiScreen.window().getGuiScaledWidth();
		}

		@Override
		public int screenHeight() {
			return guiScreen.window().getGuiScaledHeight();
		}
	}
}
