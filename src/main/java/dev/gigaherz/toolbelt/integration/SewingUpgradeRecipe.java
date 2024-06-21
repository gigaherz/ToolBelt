package dev.gigaherz.toolbelt.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

public class SewingUpgradeRecipe extends SewingRecipe
{
    public static SewingRecipeBuilder builder(Item result, CompoundTag tag)
    {
        return builder(result, 1, tag);
    }

    public static SewingRecipeBuilder builder(Item result, int count, @Nullable CompoundTag tag)
    {
        return new SewingUpgradeRecipeBuilder(result, count, tag);
    }


    public static final Codec<SewingUpgradeRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
        return SewingRecipe.defaultSewingFields(instance)
                .apply(instance, SewingUpgradeRecipe::new);
    });

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public SewingUpgradeRecipe(String group, NonNullList<Material> materials, Optional<Ingredient> pattern, Optional<Ingredient> tool, ItemStack output, boolean showNotification)
    {
        super(group, materials, pattern, tool, output, showNotification);
    }

    public SewingUpgradeRecipe(String group, NonNullList<Material> materials, @Nullable Ingredient pattern, @Nullable Ingredient tool, ItemStack output, boolean showNotification)
    {
        super(group, materials, pattern, tool, output, showNotification);
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        if (!super.matches(inv, worldIn))
            return false;

        int inputUpgradeLevel = 0;
        for (int i = 2; i < 6; i++)
        {
            var materialStack = inv.getItem(i);
            if (materialStack.getItem() instanceof ToolBeltItem belt)
            {
                inputUpgradeLevel = belt.getLevel(materialStack);
                break;
            }
        }

        var upgradedBelt = this.getResultItem();
        var upgradeLevel = ToolBelt.BELT.get().getLevel(upgradedBelt);
        return (inputUpgradeLevel+1) == upgradeLevel;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess)
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
        ItemStack upgradedBelt = super.assemble(inv, registryAccess);
        if (inputBelt.getCount() > 0)
        {
            CompoundTag inputTag = inputBelt.getTag();
            if (inputTag != null)
            {
                CompoundTag tag = upgradedBelt.getOrCreateTag();
                upgradedBelt.setTag(inputTag.copy().merge(tag));
            }
        }
        return upgradedBelt;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SewingKitIntegration.SEWING_UGRADE_SERIALIZER.get();
    }

    public static class Serializer extends SewingRecipe.SerializerBase<SewingUpgradeRecipe>
    {
        @Override
        public Codec<SewingUpgradeRecipe> codec()
        {
            return CODEC;
        }

        @Override
        protected SewingUpgradeRecipe makeRecipe(FriendlyByteBuf buffer, String group, NonNullList<Material> materials, Ingredient pattern, Ingredient tool, ItemStack result, boolean showNotification)
        {
            return new SewingUpgradeRecipe(group, materials, pattern, tool, result, showNotification);
        }
    }
}
