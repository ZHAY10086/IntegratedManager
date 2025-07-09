package com.davenonymous.integratedmanager.integrated.server;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.integrated.IDRegistries;
import com.davenonymous.integratedmanager.integrated.common.TypeData;
import com.davenonymous.integratedmanager.integrated.common.ValueData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class ValueTypeTranslator {
	private static final IValueType<ValueTypeBoolean.ValueBoolean> valueTypeBoolean;
	private static final IValueType<ValueTypeInteger.ValueInteger> valueTypeInteger;
	private static final IValueType<ValueTypeDouble.ValueDouble> valueTypeDouble;
	private static final IValueType<ValueTypeLong.ValueLong> valueTypeLong;
	private static final IValueType<ValueTypeString.ValueString> valueTypeString;
	private static final IValueType<ValueTypeList.ValueList<?,?>> valueTypeList;
	private static final IValueType<ValueTypeOperator.ValueOperator> valueTypeOperator;
	private static final IValueType<ValueTypeNbt.ValueNbt> valueTypeNbt;
	private static final IValueType<ValueObjectTypeBlock.ValueBlock> valueObjectTypeBlock;
	private static final IValueType<ValueObjectTypeItemStack.ValueItemStack> valueObjectTypeItemStack;
	private static final IValueType<ValueObjectTypeEntity.ValueEntity> valueObjectTypeEntity;
	private static final IValueType<ValueObjectTypeFluidStack.ValueFluidStack> valueObjectTypeFluidStack;
	private static final IValueType<ValueObjectTypeIngredients.ValueIngredients> valueObjectTypeIngredients;
	private static final IValueType<ValueObjectTypeRecipe.ValueRecipe> valueObjectTypeRecipe;

	static {
		IValueTypeRegistry valueTypeRegistry = IDRegistries.valueTypeRegistry;

		valueTypeOperator = valueTypeRegistry.getValueType(ValueTypes.OPERATOR.getUniqueName());
		valueTypeBoolean = valueTypeRegistry.getValueType(ValueTypes.BOOLEAN.getUniqueName());
		valueTypeInteger = valueTypeRegistry.getValueType(ValueTypes.INTEGER.getUniqueName());
		valueTypeDouble = valueTypeRegistry.getValueType(ValueTypes.DOUBLE.getUniqueName());
		valueTypeList = valueTypeRegistry.getValueType(ValueTypes.LIST.getUniqueName());
		valueTypeString = valueTypeRegistry.getValueType(ValueTypes.STRING.getUniqueName());
		valueTypeLong = valueTypeRegistry.getValueType(ValueTypes.LONG.getUniqueName());
		valueTypeNbt = valueTypeRegistry.getValueType(ValueTypes.NBT.getUniqueName());
		valueObjectTypeBlock = valueTypeRegistry.getValueType(ValueTypes.OBJECT_BLOCK.getUniqueName());
		valueObjectTypeItemStack = valueTypeRegistry.getValueType(ValueTypes.OBJECT_ITEMSTACK.getUniqueName());
		valueObjectTypeEntity = valueTypeRegistry.getValueType(ValueTypes.OBJECT_ENTITY.getUniqueName());
		valueObjectTypeFluidStack = valueTypeRegistry.getValueType(ValueTypes.OBJECT_FLUIDSTACK.getUniqueName());
		valueObjectTypeIngredients = valueTypeRegistry.getValueType(ValueTypes.OBJECT_INGREDIENTS.getUniqueName());
		valueObjectTypeRecipe = valueTypeRegistry.getValueType(ValueTypes.OBJECT_RECIPE.getUniqueName());

	}

	public static Optional<IVariableFacade> variableFacadeFromItemStack(ItemStack stack) {
		if(stack.isEmpty() || !stack.is(RegistryEntries.ITEM_VARIABLE)) {
			return Optional.empty();
		}

		IVariableFacadeHolder facadeHolder = stack.getCapability(Capabilities.VariableFacade.ITEM);
		if(facadeHolder == null) {
			return Optional.empty();
		}

		IVariableFacade variableFacade = facadeHolder.getVariableFacade(ValueDeseralizationContext.of(Minecraft.getInstance().level));
		if(variableFacade == null) {
			return Optional.empty();
		}

		return Optional.of(variableFacade);
	}

	public static ValueData translateValueType(IValueType<?> valueType, IValue value) throws EvaluationException {
		ValueData valueData = new ValueData(valueType);

		if(valueType.correspondsTo(valueTypeOperator)) {
			ValueTypeOperator.ValueOperator op = value.cast(valueTypeOperator);
			var operator = op.getRawValue();
			valueData.valueTranslationKey = operator.getTranslationKey();
			valueData.stringValue = operator.toString();
			for (IValueType<?> inputType : operator.getInputTypes()) {
				valueData.addInputType(inputType);
			}

			if (operator.getOutputType() != null) {
				valueData.outputType = new TypeData(operator.getOutputType());
			}
		} else if(valueType.correspondsTo(valueTypeBoolean)) {
			ValueTypeBoolean.ValueBoolean boolValue = value.cast(valueTypeBoolean);
			valueData.valueTranslationKey = boolValue.getRawValue() ? "general.integrateddynamics.true" : "general.integrateddynamics.false";
			valueData.stringValue = String.valueOf(boolValue.getRawValue());
		} else if(valueType.correspondsTo(valueTypeInteger)) {
			ValueTypeInteger.ValueInteger intValue = value.cast(valueTypeInteger);
			valueData.stringValue = String.valueOf(intValue.getRawValue());
		} else if(valueType.correspondsTo(valueTypeDouble)) {
			ValueTypeDouble.ValueDouble doubleValue = value.cast(valueTypeDouble);
			valueData.stringValue = String.valueOf(doubleValue.getRawValue());
		} else if(valueType.correspondsTo(valueTypeLong)) {
			ValueTypeLong.ValueLong longValue = value.cast(valueTypeLong);
			valueData.stringValue = String.valueOf(longValue.getRawValue());
		} else if(valueType.correspondsTo(valueTypeString)) {
			ValueTypeString.ValueString stringValue = value.cast(valueTypeString);
			valueData.stringValue = stringValue.getRawValue();
		} else if(valueType.correspondsTo(valueTypeList)) {
			ValueTypeList.ValueList<?, ?> listValue = value.cast(valueTypeList);
			int maxElements = 25; // Limit the number of elements we transfer to the client
			for(IValue listElement : listValue.getRawValue()) {
				try {
					valueData.addListValue(translateValueType(listElement.getType(), listElement));
					if(valueData.listValues.size() >= maxElements) {
						break; // Stop adding elements if we reached the limit
					}
				} catch (EvaluationException e) {
					IntegratedManager.LOGGER.error("Failed to translate list element value", e);
				}
			}
			valueData.listType = new TypeData(listValue.getRawValue().getValueType());
			valueData.stringValue = listValue.getRawValue().getLength() + " " + I18n.get(listValue.getRawValue().getValueType().getTranslationKey()) + "s";
		} else if(valueType.correspondsTo(valueTypeNbt)) {
			ValueTypeNbt.ValueNbt nbtValue = value.cast(valueTypeNbt);
			if(nbtValue.getRawValue().isPresent()) {
				valueData.stringValue = nbtValue.getRawValue().get().getAsString();
			}
		} else if(valueType.correspondsTo(valueObjectTypeBlock)) {
			ValueObjectTypeBlock.ValueBlock blockValue = value.cast(valueObjectTypeBlock);
			Optional<BlockState> rawValue = blockValue.getRawValue();
			if(rawValue.isPresent()) {
				valueData.valueTranslationKey = rawValue.get().getBlock().getDescriptionId();
				valueData.stringValue = rawValue.get().toString();
				valueData.addItemStackValue(new ItemStack(rawValue.get().getBlock()));
			}
		} else if(valueType.correspondsTo(valueObjectTypeItemStack)) {
			ValueObjectTypeItemStack.ValueItemStack itemStackValue = value.cast(valueObjectTypeItemStack);
			valueData.stringValue = itemStackValue.getRawValue().toString();
			valueData.valueTranslationKey = itemStackValue.getRawValue().getDescriptionId();
			valueData.addItemStackValue(itemStackValue.getRawValue());
		} else if(valueType.correspondsTo(valueObjectTypeEntity)) {
			ValueObjectTypeEntity.ValueEntity entityValue = value.cast(valueObjectTypeEntity);
			if(entityValue.getRawValue().isPresent()) {
				Entity entity = entityValue.getRawValue().get();
				valueData.typeTranslationKey = entity.getType().getDescriptionId();
				if(entity instanceof Player player) {
					valueData.stringValue = player.getScoreboardName();
				} else if(entity instanceof ItemEntity stackEntity) {
					ItemStack stack = stackEntity.getItem();
					valueData.valueTranslationKey = stack.getDescriptionId();
					valueData.stringValue = stack.toString();
				}
			}
		} else if(valueType.correspondsTo(valueObjectTypeFluidStack)) {
			ValueObjectTypeFluidStack.ValueFluidStack fluidStackValue = value.cast(valueObjectTypeFluidStack);
			valueData.stringValue = fluidStackValue.getRawValue().toString();
			valueData.valueTranslationKey = fluidStackValue.getRawValue().getDescriptionId();
		} else if(valueType.correspondsTo(valueObjectTypeIngredients)) {
			ValueObjectTypeIngredients.ValueIngredients ingredientsValue = value.cast(valueObjectTypeIngredients);
			valueData.stringValue = ingredientsValue.getRawValue().toString();
			if(ingredientsValue.getRawValue().isPresent()) {
				var mixedIngredients = ingredientsValue.getRawValue().get();
				for(var ingredientComponent : mixedIngredients.getComponents()) {
					for(var ingredient : mixedIngredients.getInstances(ingredientComponent)) {
						if(ingredient instanceof ItemStack ingredientStack) {
							valueData.valueTranslationKey = ingredientStack.getDescriptionId();
							valueData.addItemStackValue(ingredientStack.copy());
						} else if(ingredient instanceof FluidStack fluidStack) {
							valueData.addFluidStackValue(fluidStack.copy());
						} else if(ingredientComponent.getName().toString().equals("minecraft:energy") && ingredient instanceof Long energyAmount) {
							valueData.forgeEnergyValue += energyAmount;
						} else {
							IntegratedManager.LOGGER.warn("Unsupported ingredient component: {} type: {}", I18n.get(
								ingredientComponent.getTranslationKey()),
								ingredient.getClass().getName());
						}
					}
				}
			}

		} else if(valueType.correspondsTo(valueObjectTypeRecipe)) {
			ValueObjectTypeRecipe.ValueRecipe recipeValue = value.cast(valueObjectTypeRecipe);
			if(recipeValue.getRawValue().isEmpty()) {
				valueData.stringValue = "No recipe";
				return valueData; // No recipe, nothing to do
			}

			IRecipeDefinition recipe = recipeValue.getRawValue().get();
			if(!recipe.getOutput().isEmpty() && !recipe.getOutput().getComponents().isEmpty()) {
				var components = recipe.getOutput().getComponents();
				for(var ingredientComponent : components) {
					for(var ingredient : recipe.getOutput().getInstances(ingredientComponent)) {
						if(ingredient instanceof ItemStack ingredientStack) {
							valueData.valueTranslationKey = ingredientStack.getDescriptionId();
							valueData.addItemStackValue(ingredientStack.copy());
						} else if(ingredient instanceof FluidStack fluidStack) {
							valueData.addFluidStackValue(fluidStack.copy());
						} else if(ingredientComponent.getName().toString().equals("minecraft:energy") && ingredient instanceof Long energyAmount) {
							valueData.forgeEnergyValue += energyAmount;
						} else {
							IntegratedManager.LOGGER.warn("Unsupported recipe component: {} type: {}",
								I18n.get(ingredientComponent.getTranslationKey()),
								ingredient.getClass().getName()
							);
						}

					}
				}

			} else {
				valueData.stringValue = "Recipe with no output";
			}
		}

		return valueData;
	}
}
