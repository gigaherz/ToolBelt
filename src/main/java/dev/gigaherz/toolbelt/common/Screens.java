package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

public class Screens
{
    public static void openBeltScreen(ServerPlayerEntity player, int slot)
    {
        final ItemStack heldItem = player.inventory.getItem(slot);
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ToolBeltItem)
        {
            NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
                    (i, playerInventory, playerEntity) -> {
                        int blockedSlot = -1;
                        if (player.getMainHandItem() == heldItem)
                            blockedSlot = playerInventory.selected;

                        return new BeltContainer(i, playerInventory, blockedSlot, heldItem);
                    },
                    heldItem.getHoverName()
            ), (data) -> {
                data.writeVarInt(slot);
                data.writeItem(heldItem);
            });
        }
    }

    public static void openSlotScreen(ServerPlayerEntity player)
    {
        player.openMenu(new SimpleNamedContainerProvider(
                (i, playerInventory, playerEntity) -> new BeltSlotContainer(i, playerInventory, !playerEntity.level.isClientSide),
                new TranslationTextComponent("container.crafting")
        ));
    }
}
