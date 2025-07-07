package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.integrated.UnknownThings;
import com.davenonymous.integratedmanager.networking.NetworkHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PartData {
	public ResourceLocation uniqueName = UnknownThings.Part;
	public boolean writer = false;
	public boolean reader = false;
	public BlockPos targetPos = BlockPos.ZERO;
	public Direction targetSide = null;
	public ItemStack targetStack = ItemStack.EMPTY;
	public String level;
	public ResourceLocation activeAspect = UnknownThings.Aspect;
	public Map<String, ValueData> activeAspectProperties = new HashMap<>();
	public String partClassName = "UnknownPartClass";
	public String translationKey = "";

	public PartData() {
	}

	public String getBestName() {
		return I18n.exists(this.translationKey) ? I18n.get(this.translationKey) : this.targetStack.getHoverName().getString();
	}

	public PartData(RegistryFriendlyByteBuf buf) {
		this.uniqueName = buf.readResourceLocation();
		this.writer = buf.readBoolean();
		this.reader = buf.readBoolean();
		if (buf.readBoolean()) {
			this.partClassName = buf.readUtf(256);
		}
		if (buf.readBoolean()) {
			this.targetPos = buf.readBlockPos();
		}
		if (buf.readBoolean()) {
			this.targetSide = Direction.from3DDataValue(buf.readByte());
		}
		if (buf.readBoolean()) {
			this.targetStack = ItemStack.STREAM_CODEC.decode(buf);
		}
		if (buf.readBoolean()) {
			this.level = buf.readUtf(256);
		}
		if (buf.readBoolean()) {
			this.translationKey = buf.readUtf(256);
		}

		this.activeAspect = buf.readResourceLocation();

		this.activeAspectProperties = NetworkHelper.readMap(buf, HashMap::new, FriendlyByteBuf::readUtf, ValueData.STREAM_CODEC);
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeResourceLocation(uniqueName);
		buf.writeBoolean(writer);
		buf.writeBoolean(reader);
		if (partClassName != null && !partClassName.isEmpty()) {
			buf.writeBoolean(true);
			buf.writeUtf(partClassName, 256); // Write part class name, if present
		} else {
			buf.writeBoolean(false);
		}
		if (targetPos != null) {
			buf.writeBoolean(true);
			buf.writeBlockPos(targetPos);
		} else {
			buf.writeBoolean(false);
		}
		if (targetSide != null) {
			buf.writeBoolean(true);
			buf.writeByte(targetSide.get3DDataValue());
		} else {
			buf.writeBoolean(false);
		}
		if (!targetStack.isEmpty()) {
			buf.writeBoolean(true);
			ItemStack.STREAM_CODEC.encode(buf, targetStack);
		} else {
			buf.writeBoolean(false);
		}
		if (level != null && !level.isEmpty()) {
			buf.writeBoolean(true);
			buf.writeUtf(level, 256); // Write level name, if present
		} else {
			buf.writeBoolean(false);
		}

		if (translationKey != null && !translationKey.isEmpty()) {
			buf.writeBoolean(true);
			buf.writeUtf(translationKey, 256); // Write translation key, if present
		} else {
			buf.writeBoolean(false);
		}

		buf.writeResourceLocation(activeAspect);
		NetworkHelper.writeMap(buf, activeAspectProperties, FriendlyByteBuf::writeUtf, ValueData.STREAM_CODEC);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, PartData> STREAM_CODEC =
		StreamCodec.ofMember(PartData::writeToBuffer, PartData::new);
}
