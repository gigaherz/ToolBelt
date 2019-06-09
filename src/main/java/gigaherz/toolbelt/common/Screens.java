package gigaherz.toolbelt.common;

import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Screens
{
    private static final ResourceLocation BELT = ToolBelt.location("belt");
    private static final ResourceLocation BELT_SLOT = ToolBelt.location("belt_slot");

    private static class BeltGui implements INamedContainerProvider
    {
        private final ResourceLocation id;
        private final int slot;

        private BeltGui(ResourceLocation id, int slot)
        {
            this.id = id;
            this.slot = slot;
        }

        @Nullable
        @Override
        public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity player)
        {
            ItemStack heldItem = playerInventory.getStackInSlot(slot);

            int blockedSlot = -1;
            if (player.getHeldItemMainhand() == heldItem)
                blockedSlot = playerInventory.currentItem;

            return new BeltContainer(i, playerInventory, blockedSlot, heldItem);
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return new StringTextComponent(id.toString());
        }
    }

    private static class SlotGui implements INamedContainerProvider
    {
        private final ResourceLocation id;

        private SlotGui(ResourceLocation id)
        {
            this.id = id;
        }

        @Nullable
        @Override
        public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity player)
        {
            return new BeltSlotContainer(i, playerInventory, !player.world.isRemote, player);
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return new StringTextComponent(id.toString());
        }
    }

    public static void openBeltGui(ServerPlayerEntity player, int slot)
    {
        ItemStack heldItem = player.inventory.getStackInSlot(slot);
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ItemToolBelt)
        {
            NetworkHooks.openGui(player, new BeltGui(BELT, slot), (data) -> data.writeByte(slot));
        }
    }

    public static void openSlotGui(ServerPlayerEntity player)
    {
        NetworkHooks.openGui(player, new SlotGui(BELT_SLOT));
    }
}
