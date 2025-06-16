package com.davenonymous.integratedmanager.setup;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
	public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IntegratedManager.MODID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MANAGER_TABLET_VARIABLE_COUNT_COMPONENT = DATA_COMPONENTS.registerComponentType(
		"manager_tablet_count",
		builder -> builder
			.persistent(Codec.INT)
			.networkSynchronized(ByteBufCodecs.VAR_INT)
	);

}
