package dev.gigaherz.toolbelt.integration;

import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class SewingUpgradeRecipeBuilder extends SewingRecipeBuilder
{
    public static SewingRecipeBuilder begin(Item result, CompoundTag tag)
    {
        return begin(result, 1, tag);
    }

    public static SewingRecipeBuilder begin(Item result, int count, @Nullable CompoundTag tag)
    {
        return new SewingUpgradeRecipeBuilder(result, count, tag);
    }

    protected SewingUpgradeRecipeBuilder(Item result, int count, @Nullable CompoundTag tag)
    {
        super(RecipeCategory.MISC, result, count, tag);
    }
}
