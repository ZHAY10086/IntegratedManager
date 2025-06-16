package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.IntegratedDynamicsAPI;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ValueData {
	public ResourceLocation valueType;
	public String typeTranslationKey;
	public String valueTranslationKey = "";
	public String stringValue = "";
	public CompoundTag serialized;

	public IValueType valueTypeInstance;
	public IValue valueInstance;

	public ValueDeseralizationContext context = ValueDeseralizationContext.of(Minecraft.getInstance().level);

	public ValueData(IValueType valueType, IValue value) {
		this.valueTypeInstance = valueType;
		this.valueInstance = value;

		this.valueType = valueType.getUniqueName();
		this.typeTranslationKey = valueType.getTranslationKey();
		this.serialized = new CompoundTag();
		this.serialized.put("value", valueType.serialize(context, value));
	}

	public ValueData(RegistryFriendlyByteBuf buf) {
		this.valueType = buf.readResourceLocation();
		this.typeTranslationKey = buf.readUtf();
		this.valueTranslationKey = buf.readUtf();
		this.serialized = buf.readNbt();

		IValueTypeRegistry valueTypeRegistry = IntegratedDynamicsAPI.getRegistryManager().getRegistry(IValueTypeRegistry.class);
		this.valueTypeInstance = valueTypeRegistry.getValueType(this.valueType);
		this.valueInstance = this.valueTypeInstance.deserialize(context, this.serialized.get("value"));
		this.stringValue = buf.readUtf();
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeResourceLocation(valueType);
		buf.writeUtf(typeTranslationKey);
		buf.writeUtf(valueTranslationKey);
		buf.writeNbt(serialized);
		buf.writeUtf(stringValue);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, ValueData> STREAM_CODEC = StreamCodec.ofMember(
			ValueData::writeToBuffer,
			ValueData::new
	);
}
