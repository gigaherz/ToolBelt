package dev.gigaherz.toolbelt.integration;

import dev.gigaherz.sewingkit.api.SewingMaterial;
import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class SewingUpgradeRecipeBuilder extends SewingRecipeBuilder
{
    protected SewingUpgradeRecipeBuilder(HolderLookup.RegistryLookup<Item> items, ItemStack result)
    {
        super(items, RecipeCategory.MISC, result);
    }

    @Override
    protected SewingRecipe build(String group, RecipeBookCategory recipeBookCategory, NonNullList<SewingMaterial> materials, Ingredient pattern, Ingredient tool, ItemStack result, boolean showNotification)
    {
        return new SewingUpgradeRecipe(group, recipeBookCategory, materials, pattern, tool, result, showNotification);
    }
}
