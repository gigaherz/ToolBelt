package gigaherz.toolbelt.common;

import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class GuiHandler
{
    public static final ResourceLocation BELT = ToolBelt.location("belt");
    public static final ResourceLocation BELT_SLOT = ToolBelt.location("belt_slot");

    private static class BeltGui implements IInteractionObject
    {
        private final ResourceLocation id;
        private final int slot;

        public BeltGui(ResourceLocation id, int slot)
        {
            this.id = id;
            this.slot = slot;
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player)
        {
            ItemStack heldItem = player.getHeldItem(EnumHand.values()[slot]);

            int blockedSlot = -1;
            if (player.getHeldItemMainhand() == heldItem)
                blockedSlot = player.inventory.currentItem;

            return new ContainerBelt(player.inventory, blockedSlot, heldItem);
        }

        @Override
        public String getGuiID()
        {
            return id.toString();
        }

        @Override
        public ITextComponent getName()
        {
            return new TextComponentString(id.toString());
        }

        @Override
        public boolean hasCustomName()
        {
            return false;
        }

        @Nullable
        @Override
        public ITextComponent getCustomName()
        {
            return null;
        }
    }

    private static class SlotGui implements IInteractionObject
    {
        private final ResourceLocation id;

        public SlotGui(ResourceLocation id)
        {
            this.id = id;
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player)
        {
            return new ContainerBeltSlot(player.inventory, !player.world.isRemote, player);
        }

        @Override
        public String getGuiID()
        {
            return id.toString();
        }

        @Override
        public ITextComponent getName()
        {
            return new TextComponentString(id.toString());
        }

        @Override
        public boolean hasCustomName()
        {
            return false;
        }

        @Nullable
        @Override
        public ITextComponent getCustomName()
        {
            return null;
        }
    }

    public static void openBeltGui(EntityPlayerMP player, int slot)
    {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeByte(slot);

        ItemStack heldItem = player.getHeldItem(EnumHand.values()[slot]);
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ItemToolBelt)
            NetworkHooks.openGui(player, new BeltGui(BELT, slot), data);
    }

    public static void openSlotGui(EntityPlayerMP player)
    {
        NetworkHooks.openGui(player, new SlotGui(BELT_SLOT), null);
    }

    public static class Client
    {
        @Nullable
        public static GuiScreen getClientGuiElement(FMLPlayMessages.OpenContainer message)
        {
            EntityPlayerSP player = Minecraft.getInstance().player;
            if (BELT.equals(message.getId()))
            {
                ItemStack heldItem = player.getHeldItem(EnumHand.values()[message.getAdditionalData().readByte()]);
                if (heldItem.getCount() > 0)
                {
                    int blockedSlot = -1;
                    if (player.getHeldItemMainhand() == heldItem)
                        blockedSlot = player.inventory.currentItem;

                    return new GuiBelt(player.inventory, blockedSlot, heldItem);
                }
            }
            else if (BELT_SLOT.equals(message.getId()))
            {
                return new GuiBeltSlot(player);
            }
            return null;
        }
    }
}
