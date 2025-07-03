package com.davenonymous.integratedmanager.integrated.server;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.integrated.common.*;
import com.davenonymous.integratedmanager.networking.NetworkElementInfo;
import com.davenonymous.integratedmanager.networking.NetworkMasterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.PacketDistributor;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.IntegratedDynamicsAPI;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.item.*;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.part.*;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NetworkAnalysis {
	public static final BlockCapability<INetworkCarrier, Direction> NETWORK_CARRIER = Capabilities.NetworkCarrier.BLOCK;
	public static final NetworkCapability<IPartNetwork> PART_NETWORK = Capabilities.PartNetwork.NETWORK;

	INetworkCarrier carrier;
	INetwork network;
	IPartNetwork partNetwork;

	int networkId;
	int usedVariables;
	int freeVariables;

	public List<NetworkElementData> networkElements;

	public NetworkAnalysis(ServerLevel level, BlockPos pos, Direction side) {
		BlockState state = level.getBlockState(pos);

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			IntegratedManager.LOGGER.warn("ManagerTabletItem used on a block without a BlockEntity: {}", state.getBlock().getDescriptionId());
			throw new IllegalArgumentException("BlockEntity is null at " + pos + " for block " + state.getBlock().getDescriptionId());
		}

		carrier = level.getCapability(NETWORK_CARRIER, pos, state, blockEntity, side);
		if (carrier == null) {
			IntegratedManager.LOGGER.warn("ManagerTabletItem used on a block that does not implement INetworkCarrier: {}", blockEntity.getClass());
			throw new IllegalArgumentException("BlockEntity does not implement INetworkCarrier at " + pos + " for block " + state.getBlock().getDescriptionId());
		}

		network = carrier.getNetwork();
		if (network == null) {
			IntegratedManager.LOGGER.warn("ManagerTabletItem used on a block that does not have a network: {}", blockEntity.getClass());
			throw new IllegalArgumentException("Network is null at " + pos + " for block " + state.getBlock().getDescriptionId());
		}

		Optional<IPartNetwork> optPartNetwork = network.getCapability(PART_NETWORK);
		if (optPartNetwork.isEmpty()) {
			IntegratedManager.LOGGER.warn("ManagerTabletItem used on a network that does not have a tile network: {}", network.getClass());
			throw new IllegalArgumentException("PartNetwork is null at " + pos + " for block " + state.getBlock().getDescriptionId());
		}

		partNetwork = optPartNetwork.get();
	}

	public void runAnalysis() {
 		this.networkId = network.hashCode();
		this.networkElements = new ArrayList<>();


		var idRegistryManager = IntegratedDynamicsAPI.getRegistryManager();
		var facadeHandlerRegistry = idRegistryManager.getRegistry(IVariableFacadeHandlerRegistry.class);
		if (facadeHandlerRegistry == null) {
			IntegratedManager.LOGGER.warn("No VariableFacadeHandlerRegistry found, cannot analyze variables.");
			return;
		}

		IValueTypeRegistry valueTypeRegistry = idRegistryManager.getRegistry(IValueTypeRegistry.class);
		if(valueTypeRegistry == null) {
			IntegratedManager.LOGGER.warn("No ValueTypeRegistry found, cannot analyze variables.");
			return;
		}

		IOperatorRegistry operatorRegistry = idRegistryManager.getRegistry(IOperatorRegistry.class);
		if(operatorRegistry == null) {
			IntegratedManager.LOGGER.warn("No OperatorRegistry found, cannot analyze operators.");
			return;
		}

		IAspectRegistry aspectRegistry = idRegistryManager.getRegistry(IAspectRegistry.class);
		if(aspectRegistry == null) {
			IntegratedManager.LOGGER.warn("No AspectRegistry found, cannot analyze aspects.");
			return;
		}

		DataComponentType<?> facadeComponentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(
			ResourceLocation.fromNamespaceAndPath("integrateddynamics", "variable_facade"));
		if(facadeComponentType == null) {
			IntegratedManager.LOGGER.warn("No VariableFacade component type found, cannot analyze variables.");
			return;
		}

		for(INetworkElement networkElement : network.getElements()) {
			NetworkElementData networkElementData = new NetworkElementData();
			networkElementData.channelId = networkElement.getChannel();
			networkElementData.priority = networkElement.getPriority();

			if(networkElement instanceof IIdentifiableNetworkElement identifiableNetworkElement) {
				networkElementData.group = identifiableNetworkElement.getGroup();
				networkElementData.id = identifiableNetworkElement.getId();
			}

			if (networkElement instanceof IPositionedNetworkElement positionedNetworkElement) {
				var dimPos = positionedNetworkElement.getPosition();
				networkElementData.position = dimPos.getBlockPos();
			}

			if (networkElement instanceof ISidedNetworkElement sidedNetworkElement) {
				networkElementData.side = sidedNetworkElement.getSide();
			}



			if(networkElement instanceof TileNetworkElement<?> tileNetworkElement) {
				networkElementData.tileData = new TileData();
				if(tileNetworkElement.getPos().isLoaded()) {
					var level = tileNetworkElement.getPos().getLevel(false);
					if(level != null) {
						var blockEntity = level.getBlockEntity(tileNetworkElement.getPos().getBlockPos());
						if(blockEntity instanceof BlockEntityVariablestore varStore) {
							var inventory = varStore.getInventory();
							for(int i = 0; i < inventory.getItemHandler().getSlots(); i++) {
								ItemStack stack = inventory.getItemHandler().getStackInSlot(i);
								if(stack.isEmpty() || !stack.is(RegistryEntries.ITEM_VARIABLE)) {
									continue;
								}

								if(!stack.has(facadeComponentType)) {
									freeVariables++;
								}
							}
						}
					}
				}
			}

			Optional<IVariableContainer> variableHolder = getNetworkElementCapability(networkElement, Capabilities.VariableContainer.BLOCK);
			if(variableHolder.isPresent()) {
				IVariableContainer variableContainer = variableHolder.get();
				for(IVariableFacade variableFacade : variableContainer.getVariableCache().values()) {
					var variable = variableFacade.getVariable(network, partNetwork);
					VariableData variableData = new VariableData(variableFacade, variable);

					if(variableFacade instanceof IProxyVariableFacade proxyVariableFacade) {
						variable = proxyVariableFacade.getVariable(network, partNetwork);
					}

					try {
						IValue value = variable.getValue();
						IValueType<?> valueType = value.getType();

						variableData.valueData = ValueTypeTranslator.translateValueType(valueType, value);
					} catch (EvaluationException e) {
						variableData.valueData = null;
					}

					if(variableFacade instanceof IAspectVariableFacade aspectVariableFacade) {
						ItemStack fakeVariableStack = facadeHandlerRegistry.writeVariableFacadeItem(
							new ItemStack(RegistryEntries.ITEM_VARIABLE),
							aspectVariableFacade,
							aspectRegistry
						);
						variableData.translationKey = aspectVariableFacade.getAspect().getTranslationKey();
						variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
						variableData.aspect = aspectVariableFacade.getAspect().getUniqueName();
						variableData.referencedPartIds.add(aspectVariableFacade.getPartId());
					}

					if(variableFacade instanceof IOperatorVariableFacade operatorVariableFacade) {
						ItemStack fakeVariableStack = facadeHandlerRegistry.writeVariableFacadeItem(
							new ItemStack(RegistryEntries.ITEM_VARIABLE),
							operatorVariableFacade,
							operatorRegistry
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
						ItemStack fakeVariableStack = facadeHandlerRegistry.writeVariableFacadeItem(
							new ItemStack(RegistryEntries.ITEM_VARIABLE),
							valueTypeVariableFacade,
							valueTypeRegistry
						);
						variableData.translationKey = valueTypeVariableFacade.getValueType().getTranslationKey();
						variableData.variableStack = fakeVariableStack.isEmpty() ? new ItemStack(RegistryEntries.ITEM_VARIABLE) : fakeVariableStack;
						variableData.aspect = valueTypeVariableFacade.getValueType().getUniqueName();
					}

					networkElementData.variables.add(variableData);
				}
			}

			Optional<IValueInterface> valueInterfaceBlockCapability = getNetworkElementCapability(networkElement, Capabilities.ValueInterface.BLOCK);
			if(valueInterfaceBlockCapability.isPresent()) {
				IValueInterface iValueInterface = valueInterfaceBlockCapability.get();
				try {
					Optional<IValue> optValue = iValueInterface.getValue();
					if (optValue.isPresent()) {
						IValueType<?> valueType = optValue.get().getType();
						networkElementData.valueData = ValueTypeTranslator.translateValueType(valueType, optValue.get());
					}
				} catch (EvaluationException e) {
					IntegratedManager.LOGGER.warn("Error evaluating value interface for network element: {}, {}", networkElement, e);
				}
			}

			if(networkElement instanceof IPartNetworkElement<?,?> partNetworkElement) {
				PartData partData = new PartData();
				networkElementData.partId = partNetworkElement.getId();

				IPartType<? extends IPartType<?, ? extends IPartState<?>>, ? extends IPartState<?>> part = partNetworkElement.getPart();
				partData.uniqueName = part.getUniqueName();

				PartTarget partTarget = partNetworkElement.getTarget();

				DimPos targetPos = partTarget.getTarget().getPos();
				Direction targetSide = partTarget.getTarget().getSide();
				partData.targetPos = targetPos.getBlockPos();
				partData.targetSide = targetSide;
				Level targetLevel = targetPos.getLevel(true);
				partData.level = targetPos.getLevel();
				if (targetLevel != null) {
					BlockState targetState = targetLevel.getBlockState(partData.targetPos);
					partData.targetStack = new ItemStack(targetState.getBlock());
				}

				if(part instanceof IPartTypeReader<?,?> partTypeReader) {
					partData.reader = true;
					for(IAspectRead<?, ?> aspect : partTypeReader.getReadAspects()) {
						AspectData aspectData = new AspectData(aspect);
						networkElementData.aspects.add(aspectData);
					}
				}

				if(part instanceof IPartTypeActiveVariable<?, ?> partTypeActiveVariable) {
					if(partTypeActiveVariable instanceof IPartTypeWriter<?,?> partTypeWriter) {

						partData.writer = true;
						for(IAspectWrite<?, ?> aspect : partTypeWriter.getWriteAspects()) {
							AspectData aspectData = new AspectData(aspect);
							networkElementData.aspects.add(aspectData);
						}

					}
				}

				networkElementData.partData = partData;
			}

			networkElements.add(networkElementData);
		}
	}

	public void sendAnalysis(ServerPlayer player) {
		var masterInfo = new NetworkMasterInfo(networkId, networkElements.size(), usedVariables, freeVariables);
		PacketDistributor.sendToPlayer(player, masterInfo);
		for(var element : networkElements) {
			PacketDistributor.sendToPlayer(player, new NetworkElementInfo(element));
		}
	}


	@Nullable
	public static DimPos getNetworkElementPosition(INetworkElement networkElement) {
		if (networkElement instanceof IPositionedNetworkElement) {
			return ((IPositionedNetworkElement) networkElement).getPosition();
		}
		return null;
	}

	@Nullable
	public static PartPos getNetworkElementPositionSided(INetworkElement networkElement) {
		if (networkElement instanceof ISidedNetworkElement) {
			DimPos pos = getNetworkElementPosition(networkElement);
			Direction side = ((ISidedNetworkElement) networkElement).getSide();
			return PartPos.of(pos, side);
		}
		return null;
	}

	public static <T> Optional<T> getNetworkElementCapability(INetworkElement networkElement, BlockCapability<T, Direction> capability) {
		PartPos partPos = getNetworkElementPositionSided(networkElement);
		if (partPos != null) {
			return BlockEntityHelpers.getCapability(partPos.getPos(), partPos.getSide(), capability);
		}
		DimPos pos = getNetworkElementPosition(networkElement);
		if (pos != null) {
			return BlockEntityHelpers.getCapability(pos, capability);
		}
		return Optional.empty();
	}
}
