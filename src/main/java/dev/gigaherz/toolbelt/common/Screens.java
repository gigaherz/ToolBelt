package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class Screens
{
    public static void openBeltScreen(ServerPlayer player, int slot)
    {
        final ItemStack heldItem = player.getInventory().getItem(slot).copy();
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ToolBeltItem)
        {
            NetworkHooks.openGui(player, new SimpleMenuProvider(
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
                (i, playerInventory, playerEntity) -> new BeltSlotContainer(i, playerInventory, !playerEntity.level.isClientSide),
                new TranslatableComponent("container.crafting")
        ));
    }
}
