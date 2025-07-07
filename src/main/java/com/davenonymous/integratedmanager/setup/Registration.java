package com.davenonymous.integratedmanager.setup;

import com.davenonymous.integratedmanager.setup.integrated.Analyzers;
import net.neoforged.bus.api.IEventBus;

public class Registration {
	public static void register(IEventBus modbus) {
		ModItems.ITEMS.register(modbus);
		// ModContainers.CONTAINERS.register(modbus);
		ModDataComponents.DATA_COMPONENTS.register(modbus);

		Analyzers.find();
	}
}
