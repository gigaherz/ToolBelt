package dev.gigaherz.toolbelt.belt;

import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nonnull;

/**
 * Exposed as a CAPABILITY by items that want to be accepted in special equipment slots, and optionally to provide
 * custom processing for insertion, ticking, etc.
 */
public interface IItemInBelt
{
    ItemCapability<IItemInBelt, Void> CAPABILITY = ItemCapability.createVoid(ToolBelt.location("item_in_belt"), IItemInBelt.class);

    /**
     * Runs once per tick for as long as the item remains equipped in the given slot.
     *
     * @param stack The ItemStack in the slot.
     * @param container  The belt item being referenced.
     */
    default void onWornTick(@Nonnull ItemStack stack, @Nonnull ItemStack container)
    {
    }
}
