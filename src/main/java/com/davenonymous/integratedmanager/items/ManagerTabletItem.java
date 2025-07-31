package com.davenonymous.integratedmanager.items;

import com.davenonymous.integratedmanager.integrated.server.NetworkAnalysis;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ManagerTabletItem extends Item {

	public ManagerTabletItem(Properties properties) {
		super(properties);

	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos clickedPos = context.getClickedPos();
		if(level.isClientSide) {
			return InteractionResult.SUCCESS_NO_ITEM_USED;
		}

		ServerPlayer player = (ServerPlayer) context.getPlayer();
		ServerLevel serverLevel = player.serverLevel();

		try {
			var analysis = new NetworkAnalysis(serverLevel, clickedPos, context.getClickedFace());
			analysis.runAnalysis();
			analysis.sendAnalysis(player);
		} catch (IllegalArgumentException e) {
			context.getPlayer().displayClientMessage(Component.translatable("integratedmanager.network_analysis.error"), true);
			return InteractionResult.FAIL;
		}

		return InteractionResult.SUCCESS_NO_ITEM_USED;
	}
}
