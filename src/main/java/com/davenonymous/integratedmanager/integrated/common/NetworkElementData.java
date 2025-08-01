package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.gui.search.ElementSearchables;
import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.integrated.UnknownThings;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.networking.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkElementData {
	public List<VariableData> variables = new ArrayList<>();
	public List<AspectData> aspects = new ArrayList<>();
	public AspectData activeAspect = null;
	public PartData partData = null;
	public TileData tileData = null;
	public ValueData valueData = null;
	public List<ItemStack> itemBoxItems = new ArrayList<>();

	public BlockPos position = BlockPos.ZERO;
	public Direction side = null;
	public ResourceLocation group = UnknownThings.Group;
	public int id = -1; // This is the ID of the network element, not the data ID.
	public int partId = -1;
	public int channelId = -1;
	public int priority = -1;
	public int pathId = -1;
	public Map<Integer, IntegratedConnectionType> connections = null;

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkElementData> STREAM_CODEC =
		StreamCodec.ofMember(NetworkElementData::writeToBuffer, NetworkElementData::new);

	public NetworkElementData() {
		this.connections = new HashMap<>();
	}

	public NetworkElementData(RegistryFriendlyByteBuf buf) {
		this.partId = buf.readVarInt();
		this.position = buf.readBlockPos();
		this.channelId = buf.readVarInt();
		this.priority = buf.readVarInt();
		this.pathId = buf.readVarInt();
		this.variables = NetworkHelper.readCollection(buf, ArrayList::new, VariableData.STREAM_CODEC);
		this.aspects = NetworkHelper.readCollection(buf, ArrayList::new, AspectData.STREAM_CODEC);
		this.itemBoxItems = NetworkHelper.readCollection(buf, ArrayList::new, ItemStack.STREAM_CODEC);
		this.connections = NetworkHelper.readMap(buf, HashMap::new, ByteBufCodecs.VAR_INT, IntegratedConnectionType.STREAM_CODEC);

		if(buf.readBoolean()) {
			this.activeAspect = AspectData.STREAM_CODEC.decode(buf);
		} else {
			this.activeAspect = null;
		}

		if (buf.readBoolean()) {
			this.partData = PartData.STREAM_CODEC.decode(buf);
		} else {
			this.partData = null;
		}
		if (buf.readBoolean()) {
			this.tileData = TileData.STREAM_CODEC.decode(buf);
		} else {
			this.tileData = null;
		}
		if (buf.readBoolean()) {
			this.valueData = ValueData.STREAM_CODEC.decode(buf);
		} else {
			this.valueData = null;
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(tileData != null) {
				sb.append("ID: ").append(id).append(", ");
		}
		if(partData != null) {
			sb.append("Part ID: ").append(partId).append(", ");
			sb.append(partData.partClassName).append(", ");
		}

		sb.append("Position: ").append(position).append(", ");
		sb.append("Path ID: ").append(pathId);

		return sb.toString();
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(partId);
		buf.writeBlockPos(position);
		buf.writeVarInt(channelId);
		buf.writeVarInt(priority);
		buf.writeVarInt(pathId);
		NetworkHelper.writeCollection(buf, variables, VariableData.STREAM_CODEC);
		NetworkHelper.writeCollection(buf, aspects, AspectData.STREAM_CODEC);
		NetworkHelper.writeCollection(buf, itemBoxItems, ItemStack.STREAM_CODEC);
		NetworkHelper.writeMap(buf, connections, ByteBufCodecs.VAR_INT, IntegratedConnectionType.STREAM_CODEC);

		if (activeAspect != null) {
			buf.writeBoolean(true);
			activeAspect.writeToBuffer(buf);
		} else {
			buf.writeBoolean(false);
		}

		if (partData != null) {
			buf.writeBoolean(true);
			partData.writeToBuffer(buf);
		} else {
			buf.writeBoolean(false);
		}

		if (tileData != null) {
			buf.writeBoolean(true);
			tileData.writeToBuffer(buf);
		} else {
			buf.writeBoolean(false);
		}

		if (valueData != null) {
			buf.writeBoolean(true);
			valueData.writeToBuffer(buf);
		} else {
			buf.writeBoolean(false);
		}
	}

	public AspectData getAspect(ResourceLocation aspectName) {
		for (AspectData aspect : aspects) {
			if (aspect.uniqueName.equals(aspectName)) {
				return aspect;
			}
		}
		return null;
	}

	public void updateSearchIndex(Widget owner) {
		if (partData != null) {
			partData.updateSearchIndex(owner);
		}
		if (tileData != null) {
			tileData.updateSearchIndex(owner);
		}
		if (valueData != null) {
			valueData.updateSearchIndex(owner);
		}

		if(activeAspect != null) {
			activeAspect.updateSearchIndex(owner);
		} else if (aspects != null && !aspects.isEmpty()) {
			for (AspectData aspect : aspects) {
				aspect.updateSearchIndex(owner);
			}
		}

		SearchIndex.add(ElementSearchables.IDS, String.valueOf(id), owner);
		if(partId >= 0) {
			SearchIndex.add(ElementSearchables.IDS, String.valueOf(partId), owner);
		}


	}
}
