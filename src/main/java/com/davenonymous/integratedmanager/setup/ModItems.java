package com.davenonymous.integratedmanager.setup;

import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.items.ManagerTabletItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IntegratedManager.MODID);

	public static final DeferredItem<ManagerTabletItem> MANAGER_TABLET_ITEM = ITEMS
		.register("manager_tablet", () -> new ManagerTabletItem(new Item.Properties()));
}
