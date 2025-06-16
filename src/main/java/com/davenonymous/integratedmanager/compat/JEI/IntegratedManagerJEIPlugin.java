package com.davenonymous.integratedmanager.compat.JEI;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.ManagerOverview;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class IntegratedManagerJEIPlugin implements IModPlugin {
	private static final ResourceLocation UID = IntegratedManager.resource("jei_plugin");
	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiScreenHandler(ManagerOverview.class, new ManagerOverviewScreenHandler());
	}
}
