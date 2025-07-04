package com.davenonymous.integratedmanager;

import com.davenonymous.integratedmanager.setup.Registration;
import com.davenonymous.integratedmanager.setup.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(IntegratedManager.MODID)
public class IntegratedManager {

	public static final String MODID = "integratedmanager";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static ModContainer CONTAINER;

	public static ResourceLocation resource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public IntegratedManager(IEventBus modEventBus, ModContainer modContainer)
	{
		CONTAINER = modContainer;
		Registration.register(modEventBus);

		modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
	}
}
