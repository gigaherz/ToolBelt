package dev.gigaherz.toolbelt.integration;

import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class SewingUpgradeRecipeBuilder extends SewingRecipeBuilder
{
    protected SewingUpgradeRecipeBuilder(ItemStack result)
    {
        super(RecipeCategory.MISC, result);
    }

    @Override
    protected SewingRecipe build(String group, NonNullList<SewingRecipe.Material> materials, Ingredient pattern, Ingredient tool, ItemStack result, boolean showNotification)
    {
        return new SewingUpgradeRecipe(group, materials, pattern, tool, result, showNotification);
    }
}
