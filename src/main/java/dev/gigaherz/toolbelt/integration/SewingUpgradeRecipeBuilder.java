package dev.gigaherz.toolbelt.integration;

import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SewingUpgradeRecipeBuilder extends SewingRecipeBuilder
{
    public static SewingRecipeBuilder begin(Item result, CompoundNBT tag)
    {
        return begin(result, 1, tag);
    }

    public static SewingRecipeBuilder begin(Item result, int count, @Nullable CompoundNBT tag)
    {
        return new SewingUpgradeRecipeBuilder(result, count, tag);
    }

    protected SewingUpgradeRecipeBuilder(Item result, int count, @Nullable CompoundNBT tag)
    {
        super(result, count, tag);
    }

    @Override
    protected IFinishedRecipe createFinishedRecipe(ResourceLocation id, String group, Item result, int count, CompoundNBT tag, Ingredient tool, Ingredient pattern, List<SewingRecipe.Material> materials, Advancement.Builder advancementBuilder, ResourceLocation advancementId)
    {
        return new Result(id, group, result, count, tag, tool, pattern, materials, advancementBuilder, advancementId);
    }

    public static class Result extends SewingRecipeBuilder.Result
    {
        public Result(ResourceLocation id, String group, Item result, int count, @Nullable CompoundNBT tag, @Nullable Ingredient tool, @Nullable Ingredient pattern, List<SewingRecipe.Material> materials, Advancement.Builder advancementBuilder, ResourceLocation advancementId)
        {
            super(id, group, result, count, tag, tool, pattern, materials, advancementBuilder, advancementId);
        }

        @Override
        public IRecipeSerializer<?> getSerializer()
        {
            return ToolBelt.SEWING_UGRADE_SERIALIZER;
        }
    }
}
