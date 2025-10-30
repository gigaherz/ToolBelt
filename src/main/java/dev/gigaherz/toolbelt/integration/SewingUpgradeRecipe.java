package dev.gigaherz.toolbelt.integration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.sewingkit.SewingKitMod;
import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.sewingkit.table.SewingInput;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.Optional;

public class SewingUpgradeRecipe extends SewingRecipe
{
    public static SewingUpgradeRecipeBuilder builder(HolderLookup.RegistryLookup<Item> items, Item result, int count)
    {
        return new SewingUpgradeRecipeBuilder(items, new ItemStack(result, count));
    }

    public static SewingUpgradeRecipeBuilder builder(HolderLookup.RegistryLookup<Item> items, ItemStack result)
    {
        return new SewingUpgradeRecipeBuilder(items, result);
    }

    public static final MapCodec<SewingUpgradeRecipe> CODEC =
            RecordCodecBuilder.mapCodec((instance) -> defaultSewingFields(instance).apply(instance, SewingUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SewingUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            SewingKitMod.nullable(ByteBufCodecs.STRING_UTF8), SewingRecipe::group,
            ByteBufCodecs.registry(Registries.RECIPE_BOOK_CATEGORY), SewingRecipe::recipeBookCategory,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Material.STREAM_CODEC), SewingRecipe::getMaterials,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), SewingRecipe::getPattern,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), SewingRecipe::getTool,
            ItemStack.STREAM_CODEC, SewingRecipe::getOutput,
            ByteBufCodecs.BOOL, SewingRecipe::showNotification,
            SewingUpgradeRecipe::new
    );

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public SewingUpgradeRecipe(String group, RecipeBookCategory recipeBookCategory, NonNullList<Material> materials, Optional<Ingredient> pattern, Optional<Ingredient> tool, ItemStack output, boolean showNotification)
    {
        super(group, recipeBookCategory, materials, pattern, tool, output, showNotification);
    }

    public SewingUpgradeRecipe(String group, RecipeBookCategory recipeBookCategory, NonNullList<Material> materials, @Nullable Ingredient pattern, @Nullable Ingredient tool, ItemStack output, boolean showNotification)
    {
        super(group, recipeBookCategory, materials, pattern, tool, output, showNotification);
    }



    @Override
    public RecipeSerializer<SewingUpgradeRecipe> getSerializer()
    {
        return SewingKitIntegration.SEWING_UGRADE_SERIALIZER.get();
    }

    @Override
    public ItemStack assemble(SewingInput inv, HolderLookup.Provider provider)
    {
        ItemStack inputBelt = ItemStack.EMPTY;
        for (int i = 2; i < 6; i++)
        {
            if (inv.getItem(i).getItem() instanceof ToolBeltItem)
            {
                inputBelt = inv.getItem(i);
                break;
            }
        }
        ItemStack upgradedBelt = super.assemble(inv, provider);
        if (inputBelt.getCount() > 0)
        {
            int size = 2;
            var inputTag = inputBelt.getComponentsPatch();
            if (!inputTag.isEmpty())
            {
                size = ToolBeltItem.getBeltSize(upgradedBelt);
                upgradedBelt.applyComponents(inputTag);
            }
            ToolBeltItem.setBeltSize(upgradedBelt, Mth.clamp(size,2,9));
        }
        return upgradedBelt;
    }

    public static class Serializer implements RecipeSerializer<SewingUpgradeRecipe>
    {
        @Override
        public MapCodec<SewingUpgradeRecipe> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SewingUpgradeRecipe> streamCodec()
        {
            return STREAM_CODEC;
        }
    }
}
