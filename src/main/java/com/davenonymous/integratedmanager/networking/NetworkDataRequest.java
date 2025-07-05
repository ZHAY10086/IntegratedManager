package com.davenonymous.integratedmanager.networking;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.integrated.common.NetworkElementData;
import com.davenonymous.integratedmanager.integrated.server.NetworkAnalysis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NetworkDataRequest(BlockPos pos) implements CustomPacketPayload {
	public static final Type<NetworkDataRequest> TYPE = new Type<>(IntegratedManager.resource("network_data_request"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkDataRequest> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, NetworkDataRequest::pos,
			NetworkDataRequest::new
	);

	public static void handleOnServer(NetworkDataRequest request, IPayloadContext context) {
		BlockPos pos = request.pos();
		ServerPlayer player = (ServerPlayer) context.player();
		ServerLevel serverLevel = (ServerLevel) player.level();

		try {
			var analysis = new NetworkAnalysis(serverLevel, pos, Direction.UP);
			analysis.runAnalysis();
			analysis.sendAnalysis(player);
		} catch (IllegalArgumentException e) {
			IntegratedManager.LOGGER.warn("Network analysis failed at position {}: {}", pos, e.getMessage());
		}
	}
}
