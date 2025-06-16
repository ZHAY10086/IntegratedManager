package com.davenonymous.integratedmanager.setup;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

// @EventBusSubscriber(modid = IntegratedManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModContainers {
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, "integratedmanager");
}
