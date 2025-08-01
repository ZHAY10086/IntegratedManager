package com.davenonymous.integratedmanager.datagen;


import com.davenonymous.integratedmanager.IntegratedManager;
import com.davenonymous.integratedmanager.setup.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import java.util.concurrent.CompletableFuture;

public class DGRecipes extends RecipeProvider {
	public DGRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput recipeOutput) {


		ItemStack tabletItem = new ItemStack(ModItems.MANAGER_TABLET_ITEM.get());
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, tabletItem)
			.pattern(" s ")
			.pattern("dpd")
			.pattern(" o ")
			.define('s', RegistryEntries.BLOCK_VARIABLE_STORE.get())
			.define('p', RegistryEntries.ITEM_PORTABLE_LOGIC_PROGRAMMER.get())
			.define('d', PartTypes.DISPLAY_PANEL.getItem())
			.define('o', PartTypes.CONNECTOR_MONO.getItem())
			.unlockedBy("has_mono_connector", has(PartTypes.CONNECTOR_MONO.getItem()))
			.save(recipeOutput, IntegratedManager.resource("manager_tablet"));
	}
}
