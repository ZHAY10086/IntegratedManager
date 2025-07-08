package com.davenonymous.integratedmanager.integrated.analyzers;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.common.ValueData;
import com.davenonymous.integratedmanager.setup.integrated.INetworkAnalyzer;
import com.davenonymous.integratedmanager.setup.integrated.IntegratedManagerSupport;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integratedtunnels.core.part.PartTypeInterfacePositionedAddon;
import org.cyclops.integratedtunnels.core.part.PartTypeInterfacePositionedAddonFiltering;
import org.cyclops.integratedtunnels.part.*;

@SuppressWarnings("rawtypes")
@IntegratedManagerSupport(modid = "integratedtunnels")
public class Tunnels implements INetworkAnalyzer {
	@Override
	public void visitNetworkPart(IPartNetworkElement partNetworkElement, IPartType part, NetworkElementData gatheredData, INetwork network, IPartNetwork partNetwork) {
		if(part instanceof PartTypeInterfaceItem || part instanceof PartTypeInterfaceFilteringItem || part instanceof  PartTypeImporterItem || part instanceof PartTypeExporterItem) {
			gatheredData.partData.onItemChannel = true;
		}
		if(part instanceof PartTypeInterfaceEnergy || part instanceof PartTypeInterfaceFilteringEnergy || part instanceof PartTypeImporterEnergy || part instanceof PartTypeExporterEnergy) {
			gatheredData.partData.onEnergyChannel = true;
		}
		if(part instanceof PartTypeInterfaceFluid || part instanceof PartTypeInterfaceFilteringFluid || part instanceof PartTypeImporterFluid || part instanceof PartTypeExporterFluid) {
			gatheredData.partData.onFluidChannel = true;
		}

		if(part instanceof PartTypeInterfacePositionedAddon && partNetworkElement.getPartState() instanceof PartTypeInterfacePositionedAddon.State state) {
			int channel = state.getChannelInterface();

			gatheredData.partData.activeAspectProperties.put("gui.integratedtunnels.partsettings.channel.interface",
				new ValueData(channel).setIsDefaultValue(channel == 0));
		}

		if(part instanceof PartTypeInterfacePositionedAddonFiltering && partNetworkElement.getPartState() instanceof PartTypeInterfacePositionedAddonFiltering.State filteringState) {
			int channel = filteringState.getChannelInterface();

			gatheredData.partData.activeAspectProperties.put("gui.integratedtunnels.partsettings.channel.interface",
				new ValueData(channel).setIsDefaultValue(channel == 0));
		}
	}
}
