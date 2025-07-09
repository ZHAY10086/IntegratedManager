package com.davenonymous.integratedmanager.integrated.analyzers;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.integrated.server.ValueTypeTranslator;
import com.davenonymous.integratedmanager.setup.config.ClientGraphConfig;
import com.davenonymous.integratedmanager.setup.integrated.INetworkAnalyzer;
import com.davenonymous.integratedmanager.setup.integrated.IntegratedManagerSupport;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.Optional;

@SuppressWarnings("rawtypes")
@IntegratedManagerSupport(modid = "integratedcrafting")
public class Crafting implements INetworkAnalyzer {

	@Override
	public void visitNetworkPart(IPartNetworkElement partNetworkElement, IPartType part, NetworkElementData gatheredData, INetwork network, IPartNetwork partNetwork) {

		// Add variables inserted in the crafting interface
		if(part instanceof PartTypeInterfaceCrafting && partNetworkElement.getPartState() instanceof PartTypeInterfaceCrafting.State craftingInterfaceState) {
			int craftingChannel = craftingInterfaceState.getChannelCrafting();
			boolean disabledCraftingCheck = craftingInterfaceState.isDisableCraftingCheck();
			boolean blockingMode = craftingInterfaceState.getCraftingJobHandler().isBlockingJobsMode();
			var variableStore = craftingInterfaceState.getInventoryVariables();
			gatheredData.partData.activeAspectProperties.put("gui.integratedcrafting.partsettings.channel.interface", new ValueData(craftingChannel).setIsDefaultValue(craftingChannel == 0));
			gatheredData.partData.activeAspectProperties.put("gui.integratedcrafting.partsettings.craftingcheckdisabled", new ValueData(disabledCraftingCheck));
			gatheredData.partData.activeAspectProperties.put("gui.integratedcrafting.partsettings.blockingmode", new ValueData(blockingMode));

			for(ItemStack stack : variableStore.getItemStacks()) {
				Optional<IVariableFacade> optVariableFacade = ValueTypeTranslator.variableFacadeFromItemStack(stack);
				if(optVariableFacade.isEmpty()) {
					continue;
				}

				VariableData variableData = VariableData.fromFacade(optVariableFacade.get(), network, partNetwork);

				if(ClientGraphConfig.showRecipeVariables || !variableData.isRecipe()) {
					gatheredData.variables.add(variableData);
				}

				ValueData variableValue = variableData.valueData;
				if(variableValue.itemStackValues != null && !variableValue.itemStackValues.isEmpty()) {
					// If we are not showing recipe variables, we still want to show the item stacks that are in the crafting interface
					gatheredData.itemBoxItems.addAll(variableValue.itemStackValues);
				}
			}
		}

	}
}
