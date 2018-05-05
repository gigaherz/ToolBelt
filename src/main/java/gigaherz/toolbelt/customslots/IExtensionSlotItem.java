package gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableSet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Exposed as a CAPABILITY by items that want to be accepted in special equipment slots, and optionally to provide
 * custom processing for insertion, ticking, etc.
 */
public interface IExtensionSlotItem
{
    /**
     * Returns the list of slot IDs for extension containers. "forge:any" should be accepted by all extension containers
     * and is the default return type. Should be used by extension containers to test for equipability, and to display
     * in tooltips.
     * Example:
     * return ImmutableList.of(new ResourceLocation("baubles:belt"), new ResourceLocation("toolbelt:pocket"))
     *
     * @param stack The ItemStack for which the acceptable slots are being requested.
     * @return An immutable list with the ResourceLocations of the slots.
     */
    @Nonnull
    default ImmutableSet<ResourceLocation> getAcceptableSlots(@Nonnull ItemStack stack)
    {
        return CapabilityExtensionSlotItem.ANY_SLOT_LIST;
    }

    /**
     * Runs once per tick for as long as the item remains equipped in the given slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onWornTick(@Nonnull ItemStack stack, @Nonnull IExtensionSlot slot)
    {
    }

    /**
     * Called when the item is equipped to an extension slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onEquipped(@Nonnull ItemStack stack, @Nonnull IExtensionSlot slot)
    {
    }

    /**
     * Called when the item is removed from an extension slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onUnequipped(@Nonnull ItemStack stack, @Nonnull IExtensionSlot slot)
    {
    }

    /**
     * Queries wether or not the stack can be placed in the slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default boolean canEquip(@Nonnull ItemStack stack, @Nonnull IExtensionSlot slot)
    {
        return true;
    }

    /**
     * Queries wether or not the stack can be removed from the slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default boolean canUnequip(@Nonnull ItemStack stack, @Nonnull IExtensionSlot slot)
    {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack) <= 0;
    }
}
