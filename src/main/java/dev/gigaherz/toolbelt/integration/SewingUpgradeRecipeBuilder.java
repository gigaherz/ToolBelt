package dev.gigaherz.toolbelt.integration;

import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

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
        super(result, count, tag);
    }

    @Override
    protected FinishedRecipe createFinishedRecipe(ResourceLocation id, String group, Item result, int count, CompoundTag tag, Ingredient tool, Ingredient pattern, List<SewingRecipe.Material> materials, Advancement.Builder advancementBuilder, ResourceLocation advancementId)
    {
        return new Result(id, group, result, count, tag, tool, pattern, materials, advancementBuilder, advancementId);
    }

    public static class Result extends SewingRecipeBuilder.Result
    {
        public Result(ResourceLocation id, String group, Item result, int count, @Nullable CompoundTag tag, @Nullable Ingredient tool, @Nullable Ingredient pattern, List<SewingRecipe.Material> materials, Advancement.Builder advancementBuilder, ResourceLocation advancementId)
        {
            super(id, group, result, count, tag, tool, pattern, materials, advancementBuilder, advancementId);
        }

        @Override
        public RecipeSerializer<?> getType()
        {
            return ToolBelt.SEWING_UGRADE_SERIALIZER.get();
        }
    }
}
