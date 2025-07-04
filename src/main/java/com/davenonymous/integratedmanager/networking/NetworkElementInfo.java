package com.davenonymous.integratedmanager.networking;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.AllElementsReceivedEvent;
import com.davenonymous.integratedmanager.gui.ManagerOverview;
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
		NetworkData.cache().addElementData(message.data());
		IntegratedManager.LOGGER.info("Received network data info: ID={}", message.data());
		if(Minecraft.getInstance().screen instanceof ManagerOverview managerScreen) {
			managerScreen.getOrCreateGui().fireEvent(new GuiDataUpdatedEvent());
		}

		boolean haveAllParts = NetworkData.cache().elementDataList.size() == NetworkData.cache().totalParts;

		if(haveAllParts) {
			NetworkData.cache().inferValues();

			if(Minecraft.getInstance().screen instanceof ManagerOverview managerScreen) {
				managerScreen.getOrCreateGui().fireEvent(new AllElementsReceivedEvent());
			}
			IntegratedManager.LOGGER.info("All element received for network ID {}, total: {}",
					NetworkData.cache().networkId, NetworkData.cache().elementDataList.size());
		}
	}
}
