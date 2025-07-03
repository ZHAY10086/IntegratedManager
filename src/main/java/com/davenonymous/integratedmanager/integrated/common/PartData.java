package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PartData {
	public ResourceLocation uniqueName = ResourceLocation.fromNamespaceAndPath("integratedmanager", "unknown_part");
	public boolean writer = false;
	public boolean reader = false;
	public BlockPos targetPos = BlockPos.ZERO;
	public Direction targetSide = null;
	public ItemStack targetStack = ItemStack.EMPTY;
	public String level;

	public PartData() {
	}

	public PartData(RegistryFriendlyByteBuf buf) {
		this.uniqueName = buf.readResourceLocation();
		this.writer = buf.readBoolean();
		this.reader = buf.readBoolean();
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
			this.level = buf.readUtf(256); // Read level name, if present
		}
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeResourceLocation(uniqueName);
		buf.writeBoolean(writer);
		buf.writeBoolean(reader);
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
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, PartData> STREAM_CODEC =
		StreamCodec.ofMember(PartData::writeToBuffer, PartData::new);
}
