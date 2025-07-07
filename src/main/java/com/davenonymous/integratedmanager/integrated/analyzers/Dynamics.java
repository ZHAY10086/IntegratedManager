package com.davenonymous.integratedmanager.integrated.analyzers;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.VariableData;
import com.davenonymous.integratedmanager.integrated.server.ValueTypeTranslator;
import com.davenonymous.integratedmanager.setup.integrated.INetworkAnalyzer;
import com.davenonymous.integratedmanager.setup.integrated.IntegratedManagerSupport;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

import java.util.Optional;

@SuppressWarnings("rawtypes")
@IntegratedManagerSupport(modid = "integrateddynamics")
public class Dynamics implements INetworkAnalyzer {
	@Override
	public void visitNetworkTile(TileNetworkElement tileElement, BlockEntity tileEntity, NetworkElementData gatheredData, INetwork network, IPartNetwork partNetwork) {
		if(tileEntity instanceof BlockEntityProxy proxy) {
			var tileData = gatheredData.tileData;
			tileData.proxyId = proxy.getProxyId();
			var inventory = proxy.getInventory();

			for(int i = 0; i < inventory.getItemHandler().getSlots(); i++) {
				ItemStack stack = inventory.getItemHandler().getStackInSlot(i);
				Optional<IVariableFacade> optVariableFacade = ValueTypeTranslator.variableFacadeFromItemStack(stack);
				if(optVariableFacade.isEmpty()) {
					continue;
				}

				VariableData tileVariableData = VariableData.fromFacade(optVariableFacade.get(), network, partNetwork);
				gatheredData.variables.add(tileVariableData);
			}
		}

	}
}
