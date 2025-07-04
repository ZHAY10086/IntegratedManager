package com.davenonymous.integratedmanager.integrated;

import com.davenonymous.integratedmanager.IntegratedManager;
import org.cyclops.cyclopscore.init.RegistryManager;
import org.cyclops.integrateddynamics.api.IntegratedDynamicsAPI;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;

public class IDRegistries {
	public static final RegistryManager idRegistryManager = IntegratedDynamicsAPI.getRegistryManager();
	public static final IVariableFacadeHandlerRegistry facadeHandlerRegistry = idRegistryManager.getRegistry(IVariableFacadeHandlerRegistry.class);
	public static final IValueTypeRegistry valueTypeRegistry = idRegistryManager.getRegistry(IValueTypeRegistry.class);
	public static final IOperatorRegistry operatorRegistry = idRegistryManager.getRegistry(IOperatorRegistry.class);
	public static final IAspectRegistry aspectRegistry = idRegistryManager.getRegistry(IAspectRegistry.class);
}
