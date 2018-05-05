package gigaherz.toolbelt.common;

import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.belt.ToolBeltInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler
{
    public static final int BELT = 0;
    public static final int BELT_SLOT = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id)
        {
            case BELT:
                ItemStack heldItem = player.getHeldItem(EnumHand.values()[x]);
                if (heldItem.getCount() > 0)
                {
                    int blockedSlot = -1;
                    if (player.getHeldItemMainhand() == heldItem)
                        blockedSlot = player.inventory.currentItem;

                    return new ContainerBelt(player.inventory, blockedSlot, heldItem);
                }
                break;
            case BELT_SLOT:
                return new ContainerBeltSlot(player.inventory, !player.world.isRemote, player);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id)
        {
            case BELT:
                ItemStack heldItem = player.getHeldItem(EnumHand.values()[x]);
                if (heldItem.getCount() > 0)
                {
                    int blockedSlot = -1;
                    if (player.getHeldItemMainhand() == heldItem)
                        blockedSlot = player.inventory.currentItem;

                    return new GuiBelt(player.inventory, blockedSlot, heldItem);
                }
                break;
            case BELT_SLOT:
                return new GuiBeltSlot(player);
        }
        return null;
    }
}
