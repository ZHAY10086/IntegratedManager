package com.davenonymous.integratedmanager.setup;

import com.davenonymous.integratedmanager.IntegratedManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = IntegratedManager.MODID, value = Dist.CLIENT)
public class ModClientEventHandlers {
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		IntegratedManager.CONTAINER.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
}
