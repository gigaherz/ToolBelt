package dev.gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableSet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public interface IExtensionSlot
{
    // Context
    @Nonnull
    IExtensionContainer getContainer();

    @Nonnull
    ResourceLocation getType();

    // Access
    @Nonnull
    ItemStack getContents();

    void setContents(@Nonnull ItemStack stack);

    void onContentsChanged();

    // Permissions

    /**
     * Queries wether or not the stack can be placed in this slot.
     *
     * @param stack The ItemStack in the slot.
     */
    default boolean canEquip(@Nonnull ItemStack stack)
    {
        return stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null)
                .map((extItem) -> IExtensionSlot.isAcceptableSlot(this, stack, extItem) && extItem.canEquip(stack, this)).orElse(false);
    }

    /**
     * Queries wether or not the stack can be removed from this slot.
     *
     * @param stack The ItemStack in the slot.
     */
    default boolean canUnequip(@Nonnull ItemStack stack)
    {
        return stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null)
                .map((extItem) -> extItem.canUnequip(stack, this) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) <= 0).orElse(true);
    }

    static boolean isAcceptableSlot(@Nonnull IExtensionSlot slot, @Nonnull ItemStack stack, @Nonnull IExtensionSlotItem extItem)
    {
        ImmutableSet<ResourceLocation> slots = extItem.getAcceptableSlots(stack);
        return slots.contains(ExtensionSlotItemCapability.ANY_SLOT) || slots.contains(slot.getType());
    }
}
