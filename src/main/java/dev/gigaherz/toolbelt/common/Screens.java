package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.slot.BeltSlotMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkHooks;

public class Screens
{
    public static void openBeltScreen(ServerPlayer player, int slot)
    {
        final ItemStack heldItem = player.getInventory().getItem(slot).copy();
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ToolBeltItem)
        {
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (i, playerInventory, playerEntity) -> new BeltContainer(i, playerInventory, slot, heldItem),
                    heldItem.getHoverName()
            ), (data) -> {
                data.writeVarInt(slot);
                data.writeItem(heldItem);
            });
        }
    }

    public static void openSlotScreen(ServerPlayer player)
    {
        player.openMenu(new SimpleMenuProvider(
                (i, playerInventory, playerEntity) -> new BeltSlotMenu(i, playerInventory),
                Component.translatable("container.crafting")
        ));
    }
}
