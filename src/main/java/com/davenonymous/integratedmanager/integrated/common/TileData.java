package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class TileData {

	public TileData() {
	}

	public TileData(RegistryFriendlyByteBuf buf) {
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, TileData> STREAM_CODEC =
		StreamCodec.ofMember(TileData::writeToBuffer, TileData::new);

}
