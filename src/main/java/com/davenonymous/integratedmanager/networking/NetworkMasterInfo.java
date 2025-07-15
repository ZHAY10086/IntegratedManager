package com.davenonymous.integratedmanager.networking;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.ManagerOverview;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.lib.gui.event.GuiDataUpdatedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NetworkMasterInfo(BlockPos pos, int networkId, int partCount) implements CustomPacketPayload {
	public static final Type<NetworkMasterInfo> TYPE = new Type<>(IntegratedManager.resource("network_master_info"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMasterInfo> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, NetworkMasterInfo::pos,
			ByteBufCodecs.VAR_INT, NetworkMasterInfo::networkId,
			ByteBufCodecs.VAR_INT, NetworkMasterInfo::partCount,
			NetworkMasterInfo::new
	);

	public static void handleOnClient(NetworkMasterInfo message, IPayloadContext context) {
		NetworkData.cache().reset();
		NetworkData.cache().masterPosition = message.pos();
		NetworkData.cache().networkId = message.networkId();
		NetworkData.cache().totalParts = message.partCount();
		if(Minecraft.getInstance().screen instanceof ManagerOverview managerScreen) {
			managerScreen.getOrCreateGui().fireEvent(new GuiDataUpdatedEvent());
		}
		IntegratedManager.LOGGER.debug("Received network master info: ID={}, Parts={}", message.networkId(), message.partCount());

	}
}
