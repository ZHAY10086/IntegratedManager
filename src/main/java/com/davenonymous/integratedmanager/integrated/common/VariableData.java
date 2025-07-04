package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.integrated.IDRegistries;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.server.ValueTypeTranslator;
import com.davenonymous.integratedmanager.networking.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.*;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

import java.util.ArrayList;
import java.util.List;

public class VariableData {

	public int id;
	public List<Integer> referencedVariableIds = new ArrayList<>();
	public List<Integer> referencedPartIds = new ArrayList<>();
	public int proxyId = -1; // Used for proxy variables, to identify the proxy part
	public int proxiedVariableId = -1; // Used for proxy variables, to identify the proxied variable

	public String facadeClassName = "unknown_facade";
	public String label;
	public ResourceLocation type;
	public ResourceLocation aspect = ResourceLocation.fromNamespaceAndPath("integratedmanager", "unknown_aspect");
	public ItemStack variableStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
	public String translationKey;
	public ValueData valueData = null;

	public List<TypeData> inputTypes = new ArrayList<>();
	public TypeData outputType = null;

	private VariableData(IVariableFacade variableFacade, IVariable<IValue> variable) {
		this.id = variableFacade.getId();

		this.label = variableFacade.getLabel() != null ? variableFacade.getLabel() : "";
		this.type = variable.getType().getUniqueName();
		this.translationKey = variable.getType().getTranslationKey();
		this.facadeClassName = variableFacade.getClass().getSimpleName();

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
		this.facadeClassName = buf.readUtf();
		this.proxyId = buf.readVarInt();

		if(buf.readBoolean()) {
			this.valueData = ValueData.STREAM_CODEC.decode(buf);
		} else {
			this.valueData = null;
		}

		if(buf.readBoolean()) {
			this.outputType = TypeData.STREAM_CODEC.decode(buf);
		} else {
			this.outputType = null;
		}

		this.inputTypes = NetworkHelper.readCollection(buf, ArrayList::new, TypeData.STREAM_CODEC);
	}

	public void addInputType(IValueType inputType) {
		this.inputTypes.add(new TypeData(inputType));
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
		buf.writeUtf(facadeClassName);
		buf.writeVarInt(proxyId);

		if (valueData != null) {
			buf.writeBoolean(true);
			TypeData.STREAM_CODEC.encode(buf, valueData);
		} else {
			buf.writeBoolean(false);
		}

		if(outputType != null) {
			buf.writeBoolean(true);
			TypeData.STREAM_CODEC.encode(buf, outputType);
		} else {
			buf.writeBoolean(false);
		}
		NetworkHelper.writeCollection(buf, inputTypes, TypeData.STREAM_CODEC);
	}

	public static VariableData fromFacade(IVariableFacade variableFacade, INetwork network, IPartNetwork partNetwork) {
		var variable = variableFacade.getVariable(network, partNetwork);
		VariableData variableData = new VariableData(variableFacade, variable);

//		if(variableFacade instanceof IProxyVariableFacade proxyVariableFacade) {
//			variable = proxyVariableFacade.getVariable(network, partNetwork);
//		}

		try {
			IValue value = variable.getValue();
			IValueType<?> valueType = value.getType();

			variableData.valueData = ValueTypeTranslator.translateValueType(valueType, value);
		} catch (EvaluationException e) {
			variableData.valueData = null;
		}

		if(variableFacade instanceof IAspectVariableFacade aspectVariableFacade) {
			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				aspectVariableFacade,
				IDRegistries.aspectRegistry
			);
			variableData.translationKey = aspectVariableFacade.getAspect().getTranslationKey();
			variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
			variableData.aspect = aspectVariableFacade.getAspect().getUniqueName();
			variableData.referencedPartIds.add(aspectVariableFacade.getPartId());
		}

		if(variableFacade instanceof IOperatorVariableFacade operatorVariableFacade) {
			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				operatorVariableFacade,
				IDRegistries.operatorRegistry
			);

			var operator = operatorVariableFacade.getOperator();

			variableData.translationKey = operator.getTranslationKey();
			variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
			variableData.aspect = operator.getUniqueName();

			for (IValueType<?> inputType : operator.getInputTypes()) {
				variableData.addInputType(inputType);
			}

			if (operator.getOutputType() != null) {
				variableData.outputType = new TypeData(operator.getOutputType());
			}

			for(var id : operatorVariableFacade.getVariableIds()) {
				variableData.referencedVariableIds.add(id);
			}
		}

		if(variableFacade instanceof IValueTypeVariableFacade<?> valueTypeVariableFacade) {
			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				valueTypeVariableFacade,
				IDRegistries.valueTypeRegistry
			);
			variableData.translationKey = valueTypeVariableFacade.getValueType().getTranslationKey();
			variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
			variableData.aspect = valueTypeVariableFacade.getValueType().getUniqueName();
		}

		if(variableFacade instanceof IProxyVariableFacade proxyVariableFacade) {
			variableData.proxyId = proxyVariableFacade.getProxyId();
			VariableData proxiedVariable = NetworkData.cache().variableDataByProxyId.get(variableData.proxyId);
			if (proxiedVariable == null) {
				IntegratedManager.LOGGER.warn("Proxy variable with ID {} not found in cache, this is likely a bug.", variableData.proxyId);
				return variableData; // Return empty variable data if the proxied variable is not found
			}
			variableData.aspect = proxiedVariable.aspect;
			variableData.valueData = proxiedVariable.valueData;
			variableData.variableStack = proxiedVariable.variableStack;
			variableData.translationKey = proxiedVariable.translationKey;

		}

		return variableData;
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, VariableData> STREAM_CODEC =
		StreamCodec.ofMember(VariableData::writeToBuffer, VariableData::new);
}
