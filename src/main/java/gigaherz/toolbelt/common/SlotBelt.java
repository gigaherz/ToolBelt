package gigaherz.toolbelt.common;

import gigaherz.toolbelt.ConfigData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SlotBelt extends Slot
{
    public SlotBelt(IInventory playerInventory, int blockedSlot, int index, int xPosition, int yPosition)
    {
        super(new IInventory(){

            ItemStack beltStack = null;
            IItemHandlerModifiable inventory = null;

            IItemHandlerModifiable findStack()
            {
                ItemStack stack = playerInventory.getStackInSlot(blockedSlot);
                if (stack != beltStack)
                {
                    beltStack = stack;
                    inventory = (IItemHandlerModifiable)(
                            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                            .orElseThrow(() -> new RuntimeException("No inventory!"))
                    );
                }
                return inventory;
            }

            @Override
            public int getSizeInventory()
            {
                return 1;
            }

            @Override
            public boolean isEmpty()
            {
                return getStackInSlot(0).getCount() <= 0;
            }

            @Override
            public ItemStack getStackInSlot(int n)
            {
                return findStack().getStackInSlot(index);
            }

            @Override
            public ItemStack decrStackSize(int n, int count)
            {
                return findStack().extractItem(index, count, false);
            }

            @Override
            public ItemStack removeStackFromSlot(int n)
            {
                ItemStack existing = getStackInSlot(0);
                setInventorySlotContents(n, ItemStack.EMPTY);
                return existing;
            }

            @Override
            public void setInventorySlotContents(int n, ItemStack stack)
            {
                findStack().setStackInSlot(index, stack);
            }

            @Override
            public int getInventoryStackLimit()
            {
                return findStack().getSlotLimit(index);
            }

            @Override
            public void markDirty()
            {

            }

            @Override
            public boolean isUsableByPlayer(PlayerEntity player)
            {
                return false;
            }

            @Override
            public void openInventory(PlayerEntity player)
            {

            }

            @Override
            public void closeInventory(PlayerEntity player)
            {

            }

            @Override
            public boolean isItemValidForSlot(int index, ItemStack stack)
            {
                return ConfigData.isItemStackAllowed(stack);
            }

            @Override
            public void clear()
            {

            }
        }, blockedSlot, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return ConfigData.isItemStackAllowed(stack);
    }

}
