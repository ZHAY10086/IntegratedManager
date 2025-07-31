package com.davenonymous.integratedmanager.networking;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.AllElementsReceivedEvent;
import com.davenonymous.integratedmanager.gui.ManagerOverview;
import com.davenonymous.integratedmanager.gui.NodeUpdateEvent;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.lib.gui.event.GuiDataUpdatedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NetworkElementInfo(NetworkElementData data) implements CustomPacketPayload {
	public static final Type<NetworkElementInfo> TYPE = new Type<>(IntegratedManager.resource("network_element_info"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkElementInfo> STREAM_CODEC = StreamCodec.composite(
			NetworkElementData.STREAM_CODEC, NetworkElementInfo::data,
			NetworkElementInfo::new
	);

	public static void handleOnClient(NetworkElementInfo message, IPayloadContext context) {
		if(!(Minecraft.getInstance().screen instanceof ManagerOverview)) {
			Minecraft.getInstance().setScreen(new ManagerOverview());
		}

		NetworkData.cache().addElementData(message.data());
		if(Minecraft.getInstance().screen instanceof ManagerOverview managerScreen) {
			managerScreen.getOrCreateGui().fireEvent(new GuiDataUpdatedEvent());
			managerScreen.getOrCreateGui().fireEvent(new NodeUpdateEvent(message.data));
		}

		boolean haveAllParts = NetworkData.cache().elementDataList.size() == NetworkData.cache().totalParts;

		if(haveAllParts) {
			NetworkData.cache().inferValues();

			int elementCount = NetworkData.cache().elementDataList.size();
			int variableCount = NetworkData.cache().variableDataById.size();
			int pathCount = NetworkData.cache().paths.values().stream().reduce(0, (sum, map) -> sum + map.size(), Integer::sum);
			IntegratedManager.LOGGER.debug("All elements received for network ID {}, Elements: {}, Variables: {}, Paths: {}",
				NetworkData.cache().networkId,
				elementCount,
				variableCount,
				pathCount
			);

			if(Minecraft.getInstance().screen instanceof ManagerOverview managerScreen) {
				managerScreen.getOrCreateGui().fireEvent(new AllElementsReceivedEvent());
			}
		}
	}
}
