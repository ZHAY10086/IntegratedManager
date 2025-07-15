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
import org.cyclops.integratedscripting.api.item.IScriptVariableFacade;
import org.cyclops.integratedscripting.core.evaluate.ScriptVariableFacadeHandler;

@IntegratedManagerSupport(modid = "integratedscripting")
public class Scripting implements INetworkAnalyzer {
	@Override
	public void visitVariable(IVariableFacade variableFacade, VariableData variableData, INetwork network, IPartNetwork partNetwork) {
		if(variableFacade instanceof IScriptVariableFacade scriptVariableFacade) {
			int disk = scriptVariableFacade.getDisk();
			String member = scriptVariableFacade.getMember();

			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				scriptVariableFacade,
				ScriptVariableFacadeHandler.getInstance()
			);
			variableData.variableStack = fakeVariableStack;
			variableData.translationKey = "integratedmanager.message.integrated_script";
			variableData.valueData.valueTranslationKey = "";
			variableData.valueData.stringValue = member;
			variableData.scriptingDisk = disk;
			variableData.scriptingPath = scriptVariableFacade.getPath().toString();
		}
	}
}
