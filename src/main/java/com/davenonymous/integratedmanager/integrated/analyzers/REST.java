package com.davenonymous.integratedmanager.integrated.analyzers;

import com.davenonymous.integratedmanager.integrated.IDRegistries;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.setup.integrated.INetworkAnalyzer;
import com.davenonymous.integratedmanager.setup.integrated.IntegratedManagerSupport;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integratedrest.api.item.IHttpVariableFacade;
import org.cyclops.integratedrest.evaluate.HttpVariableFacadeHandler;

@IntegratedManagerSupport(modid = "integratedrest")
public class REST implements INetworkAnalyzer {
	@Override
	public void visitVariable(IVariableFacade variableFacade, VariableData variableData, INetwork network, IPartNetwork partNetwork) {
		if(variableFacade instanceof IHttpVariableFacade httpVariableFacade) {
			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				httpVariableFacade,
				HttpVariableFacadeHandler.getInstance()
			);
			variableData.variableStack = fakeVariableStack;
			variableData.translationKey = "integratedmanager.message.rest_variable";
		}
	}
}
