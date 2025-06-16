package com.davenonymous.integratedmanager;

import com.davenonymous.integratedmanager.setup.Registration;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(IntegratedManager.MODID)
public class IntegratedManager {

	public static final String MODID = "integratedmanager";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourceLocation resource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public IntegratedManager(IEventBus modEventBus, ModContainer modContainer)
	{
		Registration.register(modEventBus);
	}
}
