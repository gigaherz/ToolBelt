package dev.gigaherz.toolbelt.slot;

import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nonnull;

/**
 * Exposed as a CAPABILITY by items that want to be accepted in the belt slot slot, and optionally to provide
 * custom processing for insertion, ticking, etc.
 */
public interface IBeltSlotItem
{
    ItemCapability<IBeltSlotItem, Void> CAPABILITY = ItemCapability.createVoid(ToolBelt.location("extension_slot_item"), IBeltSlotItem.class);

    /**
     * Runs once per tick for as long as the item remains equipped in the given slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onWornTick(@Nonnull ItemStack stack, @Nonnull BeltAttachment slot)
    {
    }

    /**
     * Called when the item is equipped to an extension slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onEquipped(@Nonnull ItemStack stack, @Nonnull BeltAttachment slot)
    {
    }

    /**
     * Called when the item is removed from an extension slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onUnequipped(@Nonnull ItemStack stack, @Nonnull BeltAttachment slot)
    {
    }

    /**
     * Queries wether or not the stack can be placed in the slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default boolean canEquip(@Nonnull ItemStack stack, @Nonnull BeltAttachment slot)
    {
        return true;
    }

    /**
     * Queries wether or not the stack can be removed from the slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default boolean canUnequip(@Nonnull ItemStack stack, @Nonnull BeltAttachment slot)
    {
        return stack.getEnchantmentLevel(Enchantments.BINDING_CURSE) <= 0;
    }
}
