package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.lib.gui.widgets.WidgetNodeGraph;
import com.davenonymous.integratedmanager.networking.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class NetworkElementData {
	public List<VariableData> variables = new ArrayList<>();
	public List<AspectData> aspects = new ArrayList<>();
	public AspectData activeAspect = null;
	public PartData partData = null;
	public TileData tileData = null;
	public ValueData valueData = null;

	public BlockPos position = BlockPos.ZERO;
	public Direction side = null;
	public ResourceLocation group = ResourceLocation.fromNamespaceAndPath("integratedmanager", "unknown_group");
	public int id = -1; // This is the ID of the network element, not the data ID.
	public int partId = -1;
	public int channelId = -1;
	public int priority = -1;

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkElementData> STREAM_CODEC =
		StreamCodec.ofMember(NetworkElementData::writeToBuffer, NetworkElementData::new);

	public NetworkElementData() {
	}

	public NetworkElementData(RegistryFriendlyByteBuf buf) {
		this.partId = buf.readVarInt();
		this.position = buf.readBlockPos();
		this.channelId = buf.readVarInt();
		this.priority = buf.readVarInt();
		this.variables = NetworkHelper.readCollection(buf, ArrayList::new, VariableData.STREAM_CODEC);
		this.aspects = NetworkHelper.readCollection(buf, ArrayList::new, AspectData.STREAM_CODEC);
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

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(partId);
		buf.writeBlockPos(position);
		buf.writeVarInt(channelId);
		buf.writeVarInt(priority);
		NetworkHelper.writeCollection(buf, variables, VariableData.STREAM_CODEC);
		NetworkHelper.writeCollection(buf, aspects, AspectData.STREAM_CODEC);

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
}
