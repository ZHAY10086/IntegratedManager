package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import net.minecraft.client.resources.language.I18n;
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

	public void updateSearchIndex(Widget owner) {
		if(tileStack != null && !tileStack.isEmpty()) {
			SearchIndex.add(I18n.get(tileStack.getDescriptionId()), owner);
		}

		if(proxyId >= 0) {
			SearchIndex.add("Proxy " + proxyId, owner);
		}

		if (level != null && !level.isEmpty()) {
			SearchIndex.add(level, owner);
		}

	}

	public static final StreamCodec<RegistryFriendlyByteBuf, TileData> STREAM_CODEC =
		StreamCodec.ofMember(TileData::writeToBuffer, TileData::new);

}
