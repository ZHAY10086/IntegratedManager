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
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NetworkAnalysis {
	public static final BlockCapability<INetworkCarrier, Direction> NETWORK_CARRIER = Capabilities.NetworkCarrier.BLOCK;
	public static final NetworkCapability<IPartNetwork> PART_NETWORK = Capabilities.PartNetwork.NETWORK;

	BlockPos pos;
	INetworkCarrier carrier;
	INetwork network;
	IPartNetwork partNetwork;

	int networkId;
	int usedVariables;
	int freeVariables;

	public List<NetworkElementData> networkElements;

	public NetworkAnalysis(ServerLevel level, BlockPos pos, Direction side) {
		this.pos = pos;

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
				var tileData = new TileData();
				Level level = tileNetworkElement.getPos().getLevel(true);
				if(level != null) {
					tileData.level = level.dimension().location().toString();
					if(tileNetworkElement.getPos().isLoaded()) {
						var blockState = level.getBlockState(tileNetworkElement.getPos().getBlockPos());
						if(!blockState.isAir()) {
							tileData.tileStack = new ItemStack(blockState.getBlock());
						}

						var blockEntity = level.getBlockEntity(tileNetworkElement.getPos().getBlockPos());
						if(blockEntity != null) {
							tileData.blockEntityClass = blockEntity.getClass().getSimpleName();
						}

						if(blockEntity instanceof BlockEntityProxy proxy) {
							tileData.proxyId = proxy.getProxyId();
							var inventory = proxy.getInventory();

							for(int i = 0; i < inventory.getItemHandler().getSlots(); i++) {
								ItemStack stack = inventory.getItemHandler().getStackInSlot(i);
								Optional<IVariableFacade> optVariableFacade = ValueTypeTranslator.variableFacadeFromItemStack(stack);
								if(optVariableFacade.isEmpty()) {
									continue;
								}

								VariableData tileVariableData = VariableData.fromFacade(optVariableFacade.get(), network, partNetwork);
								networkElementData.variables.add(tileVariableData);
							}
						}
					}
				}

				networkElementData.tileData = tileData;
			}

			Optional<IVariableContainer> variableHolder = getNetworkElementCapability(networkElement, Capabilities.VariableContainer.BLOCK);
			if(variableHolder.isPresent()) {
				IVariableContainer variableContainer = variableHolder.get();
				for(IVariableFacade variableFacade : variableContainer.getVariableCache().values()) {
					VariableData variableData = VariableData.fromFacade(variableFacade, network, partNetwork);
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

				if(part instanceof PartTypeInterfaceCrafting) {
					if(partNetworkElement.getPartState() instanceof PartTypeInterfaceCrafting.State craftingInterfaceState) {
						int craftingChannel = craftingInterfaceState.getChannelCrafting();
						boolean disabledCraftingCheck = craftingInterfaceState.isDisableCraftingCheck();
						boolean blockingMode = craftingInterfaceState.getCraftingJobHandler().isBlockingJobsMode();
						var variableStore = craftingInterfaceState.getInventoryVariables();
						partData.activeAspectProperties.put("gui.integratedcrafting.partsettings.channel.interface", new ValueData(craftingChannel));
						partData.activeAspectProperties.put("gui.integratedcrafting.partsettings.craftingcheckdisabled", new ValueData(disabledCraftingCheck));
						partData.activeAspectProperties.put("gui.integratedcrafting.partsettings.blockingmode", new ValueData(blockingMode));

						for(ItemStack stack : variableStore.getItemStacks()) {
							Optional<IVariableFacade> optVariableFacade = ValueTypeTranslator.variableFacadeFromItemStack(stack);
							if(optVariableFacade.isEmpty()) {
								continue;
							}
							VariableData tileVariableData = VariableData.fromFacade(optVariableFacade.get(), network, partNetwork);
							networkElementData.variables.add(tileVariableData);
						}
					}
				}

				if(part instanceof IPartTypeReader<?,?> partTypeReader) {
					partData.reader = true;
					for(IAspectRead<?, ?> aspect : partTypeReader.getReadAspects()) {
						AspectData aspectData = new AspectData(aspect);
						networkElementData.aspects.add(aspectData);
					}
				}

				//noinspection rawtypes
				if(part instanceof IPartTypeWriter partTypeWriter) {
					partData.writer = true;
					//noinspection rawtypes,unchecked
					IAspectWrite activeAspect = partTypeWriter.getActiveAspect(partTarget, (IPartStateWriter) partNetworkElement.getPartState());
					if(activeAspect != null) {
						partData.activeAspect = activeAspect.getUniqueName();
						IAspectProperties props = activeAspect.getProperties(partTypeWriter, partTarget, ((IPartNetworkElement<?, ?>) networkElement).getPartState());
						IAspectProperties defaultProps = activeAspect.getDefaultProperties();
						for(Object propertyObj : activeAspect.getPropertyTypes()) {
							if(propertyObj instanceof IAspectPropertyTypeInstance<?, ?> property) {
								IValue value = props.getValue(property);
								IValue defaultValue = defaultProps.getValue(property);
								try {
									ValueData valueData = ValueTypeTranslator.translateValueType(value.getType(), value);
									valueData.isDefaultValue = value.equals(defaultValue);
									partData.activeAspectProperties.put(property.getTranslationKey(), valueData);
								} catch (EvaluationException e) {
									IntegratedManager.LOGGER.warn("Error translating value type for aspect property: {}, {}", property.getTranslationKey(), e);
								}
							}
						}
					}

					for(Object aspect : partTypeWriter.getWriteAspects()) {
						AspectData aspectData = new AspectData((IAspect<?, ?>) aspect);
						networkElementData.aspects.add(aspectData);
					}
				}

				networkElementData.partData = partData;
			}

			networkElements.add(networkElementData);
		}
	}

	public void sendAnalysis(ServerPlayer player) {
		var masterInfo = new NetworkMasterInfo(pos, networkId, networkElements.size(), usedVariables, freeVariables);
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
