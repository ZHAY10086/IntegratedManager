package com.davenonymous.integratedmanager.networking;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.ManagerOverview;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.lib.gui.event.GuiDataUpdatedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NetworkMasterInfo(int networkId, int partCount, int usedVariables, int freeVariables) implements CustomPacketPayload {
	public static final Type<NetworkMasterInfo> TYPE = new Type<>(IntegratedManager.resource("network_master_info"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMasterInfo> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, NetworkMasterInfo::networkId,
			ByteBufCodecs.VAR_INT, NetworkMasterInfo::partCount,
			ByteBufCodecs.VAR_INT, NetworkMasterInfo::usedVariables,
			ByteBufCodecs.VAR_INT, NetworkMasterInfo::freeVariables,
			NetworkMasterInfo::new
	);

	public static void handleOnClient(NetworkMasterInfo message, IPayloadContext context) {
		NetworkData.cache().reset();
		NetworkData.cache().networkId = message.networkId();
		NetworkData.cache().totalParts = message.partCount();
		NetworkData.cache().usedVariables = message.usedVariables();
		NetworkData.cache().freeVariables = message.freeVariables();
		if(Minecraft.getInstance().screen instanceof ManagerOverview managerScreen) {
			managerScreen.getOrCreateGui().fireEvent(new GuiDataUpdatedEvent());
		}
		IntegratedManager.LOGGER.info("Received network master info: ID={}, Parts={}, Used Variables={}, Free Variables={}",
				message.networkId(), message.partCount(), message.usedVariables(), message.freeVariables());

	}
}
