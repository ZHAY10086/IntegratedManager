package com.davenonymous.integratedmanager.integrated.common;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.gui.search.ElementSearchables;
import com.davenonymous.integratedmanager.gui.search.SearchIndex;
import com.davenonymous.integratedmanager.integrated.IDRegistries;
import com.davenonymous.integratedmanager.integrated.UnknownThings;
import com.davenonymous.integratedmanager.integrated.client.NetworkData;
import com.davenonymous.integratedmanager.integrated.server.ValueTypeTranslator;
import com.davenonymous.integratedmanager.lib.gui.widgets.Widget;
import com.davenonymous.integratedmanager.networking.NetworkHelper;
import com.davenonymous.integratedmanager.setup.integrated.Analyzers;
import com.davenonymous.integratedmanager.setup.integrated.INetworkAnalyzer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.*;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableData {

	public int id;
	public List<Integer> referencedVariableIds = new ArrayList<>();
	public List<Integer> referencedPartIds = new ArrayList<>();
	public int proxyId = -1; // Used for proxy variables, to identify the proxy part
	public int proxiedVariableId = -1; // Used for proxy variables, to identify the proxied variable
	public int scriptingDisk = -1; // Used for scripting variables, to identify the disk

	public String scriptingPath;
	public String facadeClassName = "unknown_facade";
	public String label;
	public ResourceLocation type;
	public AspectData aspect = null;
	public ItemStack variableStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
	public String translationKey;
	public ValueData valueData = null;
	public Map<String, ValueData> aspectProperties = new HashMap<>();

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
		this.variableStack = ItemStack.STREAM_CODEC.decode(buf);
		this.translationKey = buf.readUtf();
		this.facadeClassName = buf.readUtf();
		this.proxyId = buf.readVarInt();
		this.scriptingDisk = buf.readInt();

		if(buf.readBoolean()) {
			this.scriptingPath = buf.readUtf();
		} else {
			this.scriptingPath = null;
		}

		if(buf.readBoolean()) {
			this.aspect = AspectData.STREAM_CODEC.decode(buf);
		} else {
			this.aspect = null;
		}

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
		this.aspectProperties = NetworkHelper.readMap(buf, HashMap::new, FriendlyByteBuf::readUtf, ValueData.STREAM_CODEC);
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
		ItemStack.STREAM_CODEC.encode(buf, variableStack);
		buf.writeUtf(translationKey);
		buf.writeUtf(facadeClassName);
		buf.writeVarInt(proxyId);
		buf.writeInt(scriptingDisk);

		if(scriptingPath != null) {
			buf.writeBoolean(true);
			buf.writeUtf(scriptingPath);
		} else {
			buf.writeBoolean(false);
		}

		if (aspect != null) {
			buf.writeBoolean(true);
			AspectData.STREAM_CODEC.encode(buf, aspect);
		} else {
			buf.writeBoolean(false);
		}

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
		NetworkHelper.writeMap(buf, aspectProperties, FriendlyByteBuf::writeUtf, ValueData.STREAM_CODEC);
	}

	public static VariableData fromFacade(IVariableFacade variableFacade, INetwork network, IPartNetwork partNetwork) {
		var variable = variableFacade.getVariable(network, partNetwork);
		VariableData variableData = new VariableData(variableFacade, variable);

		try {
			IValue value = variable.getValue();
			IValueType<?> valueType = value.getType();

			variableData.valueData = ValueTypeTranslator.translateValueType(valueType, value);
		} catch (EvaluationException e) {
			variableData.valueData = null;
		}

		if(variableFacade instanceof IAspectVariableFacade aspectVariableFacade) {
			var aspect = aspectVariableFacade.getAspect();
			int sourcePartId = aspectVariableFacade.getPartId();
			var sourcePartState = partNetwork.getPartState(sourcePartId);
			var aspectProperties = sourcePartState.getAspectProperties(aspect);
			IAspectProperties defaultProps = aspect.getDefaultProperties();

			for(Object propertyObj : aspect.getPropertyTypes()) {
				if(propertyObj instanceof IAspectPropertyTypeInstance<?, ?> property) {
					IValue value = aspectProperties.getValue(property);
					IValue defaultValue = defaultProps.getValue(property);
					try {
						ValueData valueData = ValueTypeTranslator.translateValueType(value.getType(), value);
						valueData.isDefaultValue = value.equals(defaultValue);
						variableData.aspectProperties.put(property.getTranslationKey(), valueData);
					} catch (EvaluationException e) {
						IntegratedManager.LOGGER.warn("Error translating value type for aspect property: {}, {}", property.getTranslationKey(), e);
					}
				}
			}

			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				aspectVariableFacade,
				IDRegistries.aspectRegistry
			);

			variableData.translationKey = aspectVariableFacade.getAspect().getTranslationKey();
			variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
			variableData.aspect = new AspectData(aspectVariableFacade.getAspect());
			variableData.referencedPartIds.add(aspectVariableFacade.getPartId());
		}

		if(variableFacade instanceof IOperatorVariableFacade operatorVariableFacade) {
			ItemStack fakeVariableStack = IDRegistries.facadeHandlerRegistry.writeVariableFacadeItem(
				new ItemStack(RegistryEntries.ITEM_VARIABLE),
				operatorVariableFacade,
				IDRegistries.operatorRegistry
			);

			IOperator operator = operatorVariableFacade.getOperator();

			variableData.translationKey = operator.getTranslationKey();
			variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
			variableData.aspect = new AspectData(operator);

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
			variableData.aspect = new AspectData(valueTypeVariableFacade.getValueType());
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

		for(INetworkAnalyzer analyzer : Analyzers.analyzers) {
			analyzer.visitVariable(variableFacade, variableData, network, partNetwork);
		}

		return variableData;
	}

	public boolean isUnused() {
		return proxyId == -1 && hasNoReferences() && isNotBeingReferenced();
	}

	public boolean isNotBeingReferenced() {
		return NetworkData.cache().variableDataById.values().stream().noneMatch(variableData -> variableData.referencedVariableIds.contains(this.id));
	}

	public boolean hasNoReferences() {
		return referencedVariableIds.isEmpty() && referencedPartIds.isEmpty();
	}

	public boolean isRecipe() {
		return this.facadeClassName.equals("ValueTypeVariableFacade") && type.equals(ValueTypes.OBJECT_RECIPE.getUniqueName());
	}

	public void updateSearchIndex(Widget owner) {
		if (valueData != null) {
			valueData.updateSearchIndex(owner);
		}

		if (translationKey != null && !translationKey.isBlank()) {
			SearchIndex.add(translationKey, owner);
		}

		if (label != null && !label.isBlank()) {
			SearchIndex.add(label, owner);
		}

		if (aspect != null) {
			aspect.updateSearchIndex(owner);
		}

		for(TypeData inputType : inputTypes) {
			String translatedType = I18n.exists(inputType.typeTranslationKey) ? I18n.get(inputType.typeTranslationKey) : inputType.valueType.toString();
			SearchIndex.add(translatedType, owner);
		}

		if(outputType != null) {
			String translatedType = I18n.exists(outputType.typeTranslationKey) ? I18n.get(outputType.typeTranslationKey) : outputType.valueType.toString();
			SearchIndex.add(translatedType, owner);
		}

		for (ValueData aspectProperty : aspectProperties.values()) {
			aspectProperty.updateSearchIndex(owner);
		}

		SearchIndex.add(ElementSearchables.IDS, String.valueOf(id), owner);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, VariableData> STREAM_CODEC =
		StreamCodec.ofMember(VariableData::writeToBuffer, VariableData::new);
}
