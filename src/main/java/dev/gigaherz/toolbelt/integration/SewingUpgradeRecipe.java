package dev.gigaherz.toolbelt.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.sewingkit.api.SewingRecipe;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Optional;

public class SewingUpgradeRecipe extends SewingRecipe
{
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
