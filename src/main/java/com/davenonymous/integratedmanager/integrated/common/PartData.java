package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.integrated.UnknownThings;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.networking.NetworkHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartData {
	public ResourceLocation uniqueName = UnknownThings.Part;
	public boolean writer = false;
	public boolean reader = false;
	public BlockPos targetPos = BlockPos.ZERO;
	public Direction targetSide = null;
	public ItemStack targetStack = ItemStack.EMPTY;
	public String level;
	public AspectData activeAspect = null;
	public Map<String, ValueData> activeAspectProperties = new HashMap<>();
	public String partClassName = "UnknownPartClass";
	public String translationKey = "";
	public List<String> errors = new ArrayList<>();
	public int omniId = -1;

	public boolean onEnergyChannel = false;
	public boolean onItemChannel = false;
	public boolean onFluidChannel = false;

	public PartData() {
	}

	public String getBestName() {
		return I18n.exists(this.translationKey) ? I18n.get(this.translationKey) : this.targetStack.getHoverName().getString();
	}

	public PartData(RegistryFriendlyByteBuf buf) {
		this.uniqueName = buf.readResourceLocation();
		this.writer = buf.readBoolean();
		this.reader = buf.readBoolean();
		this.onEnergyChannel = buf.readBoolean();
		this.onItemChannel = buf.readBoolean();
		this.onFluidChannel = buf.readBoolean();
		this.omniId = buf.readInt();

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

		if(buf.readBoolean()) {
			this.activeAspect = AspectData.STREAM_CODEC.decode(buf);
		}

		this.activeAspectProperties = NetworkHelper.readMap(buf, HashMap::new, FriendlyByteBuf::readUtf, ValueData.STREAM_CODEC);
		this.errors = NetworkHelper.readCollection(buf, ArrayList::new, FriendlyByteBuf::readUtf);
	}

	public void updateSearchIndex(Widget owner) {
		if(I18n.exists(translationKey)) {
			SearchIndex.add(I18n.get(translationKey), owner);
		}
		if(I18n.exists(translationKey + ".info")) {
			SearchIndex.add(I18n.get(translationKey + ".info"), owner);
		}

		if(activeAspect != null) {
			activeAspect.updateSearchIndex(owner);
		}

		if (!activeAspectProperties.isEmpty()) {
			for (String propertyKey : activeAspectProperties.keySet()) {
				if (I18n.exists(propertyKey)) {
					SearchIndex.add(I18n.get(propertyKey), owner);
				}
				if (I18n.exists(propertyKey + ".info")) {
					SearchIndex.add(I18n.get(propertyKey + ".info"), owner);
				}
			}
		}
		for(String error : errors) {
			if (I18n.exists(error)) {
				SearchIndex.add(I18n.get(error), owner);
			} else {
				SearchIndex.add(error, owner);
			}
		}


	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeResourceLocation(uniqueName);
		buf.writeBoolean(writer);
		buf.writeBoolean(reader);
		buf.writeBoolean(onEnergyChannel);
		buf.writeBoolean(onItemChannel);
		buf.writeBoolean(onFluidChannel);
		buf.writeInt(omniId);

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

		if (activeAspect != null) {
			buf.writeBoolean(true);
			activeAspect.writeToBuffer(buf);
		} else {
			buf.writeBoolean(false);
		}
		NetworkHelper.writeMap(buf, activeAspectProperties, FriendlyByteBuf::writeUtf, ValueData.STREAM_CODEC);
		NetworkHelper.writeCollection(buf, errors, FriendlyByteBuf::writeUtf);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, PartData> STREAM_CODEC =
		StreamCodec.ofMember(PartData::writeToBuffer, PartData::new);
}
