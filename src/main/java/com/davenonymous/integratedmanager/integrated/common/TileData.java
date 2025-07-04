package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class TileData {
	public ItemStack tileStack = ItemStack.EMPTY;
	public String level;
	public String blockEntityClass;
	public int proxyId = -1;

	public TileData() {
	}

	public TileData(RegistryFriendlyByteBuf buf) {
		if (buf.readBoolean()) {
			this.tileStack = ItemStack.STREAM_CODEC.decode(buf);
		}
		if (buf.readBoolean()) {
			this.level = buf.readUtf(256); // Read level name, if present
		}
		if (buf.readBoolean()) {
			this.blockEntityClass = buf.readUtf(256); // Read block entity class name, if present
		}
		this.proxyId = buf.readVarInt(); // Read proxy ID
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		if (!tileStack.isEmpty()) {
			buf.writeBoolean(true);
			ItemStack.STREAM_CODEC.encode(buf, tileStack);
		} else {
			buf.writeBoolean(false);
		}
		if (level != null && !level.isEmpty()) {
			buf.writeBoolean(true);
			buf.writeUtf(level, 256); // Write level name, if present
		} else {
			buf.writeBoolean(false);
		}
		if (blockEntityClass != null && !blockEntityClass.isEmpty()) {
			buf.writeBoolean(true);
			buf.writeUtf(blockEntityClass, 256); // Write block entity class name, if present
		} else {
			buf.writeBoolean(false);
		}
		buf.writeVarInt(proxyId); // Write proxy ID

	}

	public static final StreamCodec<RegistryFriendlyByteBuf, TileData> STREAM_CODEC =
		StreamCodec.ofMember(TileData::writeToBuffer, TileData::new);

}
