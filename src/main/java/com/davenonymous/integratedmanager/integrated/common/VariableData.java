package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;

import java.util.ArrayList;
import java.util.List;

public class VariableData {

	public int id;
	public List<Integer> referencedVariableIds = new ArrayList<>();
	public List<Integer> referencedPartIds = new ArrayList<>();

	public String label;
	public ResourceLocation type;
	public ResourceLocation aspect = ResourceLocation.fromNamespaceAndPath("integratedmanager", "unknown_aspect");
	public ItemStack variableStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
	public String translationKey;
	public ValueData valueData = null;

	public VariableData(IVariableFacade variableFacade, IVariable<IValue> variable) {
		this.id = variableFacade.getId();

		this.label = variableFacade.getLabel() != null ? variableFacade.getLabel() : "";
		this.type = variable.getType().getUniqueName();
		this.translationKey = variable.getType().getTranslationKey();

	}

	public VariableData(RegistryFriendlyByteBuf buf) {
		this.id = buf.readVarInt();
		this.label = buf.readUtf();
		this.referencedVariableIds = buf.readList(FriendlyByteBuf::readInt);
		this.referencedPartIds = buf.readList(FriendlyByteBuf::readInt);
		this.type = buf.readResourceLocation();
		this.aspect = buf.readResourceLocation();
		this.variableStack = ItemStack.STREAM_CODEC.decode(buf);
		this.translationKey = buf.readUtf();

		if(buf.readBoolean()) {
			this.valueData = ValueData.STREAM_CODEC.decode(buf);
		} else {
			this.valueData = null;
		}
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(id);
		buf.writeUtf(label);
		buf.writeCollection(referencedVariableIds, FriendlyByteBuf::writeInt);
		buf.writeCollection(referencedPartIds, FriendlyByteBuf::writeInt);
		buf.writeResourceLocation(type);
		buf.writeResourceLocation(aspect);
		ItemStack.STREAM_CODEC.encode(buf, variableStack);
		buf.writeUtf(translationKey);

		if (valueData != null) {
			buf.writeBoolean(true);
			ValueData.STREAM_CODEC.encode(buf, valueData);
		} else {
			buf.writeBoolean(false);
		}
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, VariableData> STREAM_CODEC =
		StreamCodec.ofMember(VariableData::writeToBuffer, VariableData::new);
}
