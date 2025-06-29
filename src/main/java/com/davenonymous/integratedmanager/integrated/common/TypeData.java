package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

public class TypeData {
	public ResourceLocation valueType;
	public String typeTranslationKey;

	public TypeData(IValueType valueType) {
		this.valueType = valueType.getUniqueName();
		this.typeTranslationKey = valueType.getTranslationKey();
	}

	public TypeData(RegistryFriendlyByteBuf buf) {
		this.valueType = buf.readResourceLocation();
		this.typeTranslationKey = buf.readUtf();
	}

	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		buf.writeResourceLocation(valueType);
		buf.writeUtf(typeTranslationKey);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, TypeData> STREAM_CODEC = StreamCodec.ofMember(
		TypeData::writeToBuffer,
		TypeData::new
	);

}
