package com.davenonymous.integratedmanager.setup.integrated;

import com.davenonymous.integratedmanager.IntegratedManager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

public class Analyzers {
	public static List<INetworkAnalyzer> analyzers = new ArrayList<>();

	public static void find() {
		analyzers.clear();

		ModFileScanData scanData = ModList.get().getModFileById(IntegratedManager.MODID).getFile().getScanResult();
		var foundAnalyzerClasses = scanData.getAnnotatedBy(IntegratedManagerSupport.class, ElementType.TYPE);

		foundAnalyzerClasses.forEach(annotationData -> {
			Object foo = annotationData.annotationData().get("modid");
			if(!(foo instanceof String modid)) {
				return;
			}

			if(!ModList.get().isLoaded(modid)) {
				return;
			}

			try {
				Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
				INetworkAnalyzer analyzer = (INetworkAnalyzer) clazz.getDeclaredConstructor().newInstance();
				analyzers.add(analyzer);

				IntegratedManager.LOGGER.info("Found analyzer class: " + annotationData.clazz().getClassName() + " for mod: " + modid);
			} catch (Exception e) {
				IntegratedManager.LOGGER.error("Failed to instantiate analyzer class: " + annotationData.clazz().getClassName(), e);
			}
		});
	}
}
