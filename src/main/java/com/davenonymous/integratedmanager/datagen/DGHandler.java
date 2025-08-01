package com.davenonymous.integratedmanager.datagen;

import com.davenonymous.integratedmanager.IntegratedManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = IntegratedManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DGHandler {
	@SuppressWarnings("ConstantConditions")
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeServer(), new DGRecipes(output, lookupProvider));
	}
}
