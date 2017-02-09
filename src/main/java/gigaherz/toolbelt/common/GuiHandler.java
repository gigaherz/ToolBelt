package gigaherz.toolbelt.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler
{
    public static final int BELT = 0;

    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> CAP;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch(id)
        {
            case BELT:
                ItemStack heldItem = player.getHeldItem(EnumHand.values()[x]);
                if (heldItem != null)
                {
                    IItemHandlerModifiable inventory = (IItemHandlerModifiable)heldItem.getCapability(CAP, null);

                    int blockedSlot = -1;
                    if (player.getHeldItemMainhand() == heldItem)
                        blockedSlot = player.inventory.currentItem;

                    return new ContainerChestItem(player.inventory, inventory, blockedSlot);
                }
                break;
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch(id)
        {
            case BELT:
                ItemStack heldItem = player.getHeldItem(EnumHand.values()[x]);
                if (heldItem != null)
                {
                    IItemHandlerModifiable inventory = (IItemHandlerModifiable)heldItem.getCapability(CAP, null);

                    int blockedSlot = -1;
                    if (player.getHeldItemMainhand() == heldItem)
                        blockedSlot = player.inventory.currentItem;

                    return new GuiChestItem(player.inventory, inventory, blockedSlot, heldItem);
                }
                break;
        }
        return null;
    }
}
