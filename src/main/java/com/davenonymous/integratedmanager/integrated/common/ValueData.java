package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.networking.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ValueData extends TypeData {
	public String valueTranslationKey = "";
	public String stringValue = "";

	public List<TypeData> inputTypes = new ArrayList<>();
	public TypeData outputType = null;

	public List<ItemStack> itemStackValues = new ArrayList<>();

	public List<ValueData> listValues = new ArrayList<>();
	public TypeData listType = null;

	public ValueData(IValueType valueType) {
		super(valueType);
	}

	public ValueData(RegistryFriendlyByteBuf buf) {
		super(buf);
		this.valueTranslationKey = buf.readUtf();
		this.stringValue = buf.readUtf();

		if(buf.readBoolean()) {
			this.outputType = TypeData.STREAM_CODEC.decode(buf);
		} else {
			this.outputType = null;
		}

		this.inputTypes = NetworkHelper.readCollection(buf, ArrayList::new, TypeData.STREAM_CODEC);
		this.itemStackValues = NetworkHelper.readCollection(buf, ArrayList::new, ItemStack.STREAM_CODEC);
		this.listValues = NetworkHelper.readCollection(buf, ArrayList::new, ValueData.STREAM_CODEC);

		if(buf.readBoolean()) {
			this.listType = TypeData.STREAM_CODEC.decode(buf);
		} else {
			this.listType = null;
		}
	}

	@Override
	public void writeToBuffer(RegistryFriendlyByteBuf buf) {
		super.writeToBuffer(buf);
		buf.writeUtf(valueTranslationKey);
		buf.writeUtf(stringValue);

		if(outputType != null) {
			buf.writeBoolean(true);
			TypeData.STREAM_CODEC.encode(buf, outputType);
		} else {
			buf.writeBoolean(false);
		}
		NetworkHelper.writeCollection(buf, inputTypes, TypeData.STREAM_CODEC);
		NetworkHelper.writeCollection(buf, itemStackValues, ItemStack.STREAM_CODEC);
		NetworkHelper.writeCollection(buf, listValues, ValueData.STREAM_CODEC);

		if (listType != null) {
			buf.writeBoolean(true);
			TypeData.STREAM_CODEC.encode(buf, listType);
		} else {
			buf.writeBoolean(false);
		}
	}

	public void addInputType(IValueType inputType) {
		this.inputTypes.add(new TypeData(inputType));
	}

	public void addItemStackValue(ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			this.itemStackValues.add(itemStack);
		}
	}

	public void addListValue(ValueData valueData) {
		if (valueData != null) {
			this.listValues.add(valueData);
		}
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, ValueData> STREAM_CODEC = StreamCodec.ofMember(
			ValueData::writeToBuffer,
			ValueData::new
	);
}
