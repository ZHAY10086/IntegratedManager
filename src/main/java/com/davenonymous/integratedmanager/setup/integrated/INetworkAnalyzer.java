package com.davenonymous.integratedmanager.setup.integrated;

import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

@SuppressWarnings("rawtypes")
public interface INetworkAnalyzer {
	default void visitNetworkElement(INetworkElement networkElement, NetworkElementData gatheredData, INetwork network, IPartNetwork partNetwork) {}

	default void visitNetworkTile(TileNetworkElement tileElement, BlockEntity tileEntity, NetworkElementData gatheredData, INetwork network, IPartNetwork partNetwork) {
		// Default implementation does nothing
	}

	default void visitNetworkPart(IPartNetworkElement partNetworkElement, IPartType part, NetworkElementData gatheredData, INetwork network, IPartNetwork partNetwork) {
		// Default implementation does nothing
	}
}
