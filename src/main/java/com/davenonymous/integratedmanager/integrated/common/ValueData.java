package com.davenonymous.integratedmanager.integrated.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ValueData extends TypeData {
	public String valueTranslationKey = "";
	public String stringValue = "";

	public ValueData(IValueType valueType) {
		super(valueType);
	}

	public ValueData(RegistryFriendlyByteBuf buf) {
		super(buf);
		this.valueTranslationKey = buf.readUtf();
		this.stringValue = buf.readUtf();
	}

	@Override
	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		super.writeToBuffer(buf);
		buf.writeUtf(valueTranslationKey);
		buf.writeUtf(stringValue);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, ValueData> STREAM_CODEC = StreamCodec.ofMember(
			ValueData::writeToBuffer,
			ValueData::new
	);
}
