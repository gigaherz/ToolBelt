package gigaherz.toolbelt.common;

import gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Screens
{
    public static void openBeltScreen(ServerPlayerEntity player, int slot)
    {
        final ItemStack heldItem = player.inventory.getStackInSlot(slot);
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ToolBeltItem)
        {
            NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
                    (i, playerInventory, playerEntity) -> {
                        int blockedSlot = -1;
                        if (player.getHeldItemMainhand() == heldItem)
                            blockedSlot = playerInventory.currentItem;

                        return new BeltContainer(i, playerInventory, blockedSlot, heldItem);
                    },
                    heldItem.getDisplayName()
            ), (data) -> {
                data.writeVarInt(slot);
                data.writeItemStack(heldItem);
            });
        }
    }

    public static void openSlotScreen(ServerPlayerEntity player)
    {
        player.openContainer(new SimpleNamedContainerProvider(
                (i, playerInventory, playerEntity) -> new BeltSlotContainer(i, playerInventory, !playerEntity.world.isRemote),
                new TranslationTextComponent("container.crafting")
        ));
    }
}
