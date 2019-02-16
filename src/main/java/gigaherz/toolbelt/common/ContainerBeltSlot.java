package gigaherz.toolbelt.common;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.customslots.SlotExtension;

public class ContainerBeltSlot extends ContainerPlayer
{
    public static final ResourceLocation EMPTY_SPRITE = ToolBelt.location("gui/empty_belt_slot_background");

    private final SlotExtension slotBelt;
    private final IExtensionSlot extensionSlot;

    public ContainerBeltSlot(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer playerIn)
    {
        super(playerInventory, localWorld, playerIn);

        ExtensionSlotBelt container = playerIn.getCapability(ExtensionSlotBelt.CAPABILITY)
                .orElseThrow(() -> new RuntimeException("Item handler not present."));

        extensionSlot = container.getBelt();

        this.addSlot(slotBelt = new SlotExtension(extensionSlot, 77, 44));
        slotBelt.setBackgroundName(EMPTY_SPRITE.toString());
    }

    public void addListener(IContainerListener listener) {
        super.addListener(listener);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote)
            BeltFinder.sendSync(playerIn);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack remaining = ItemStack.EMPTY;
            ItemStack slotContents = slot.getStack();
            remaining = slotContents.copy();

            if (index == slotBelt.slotNumber)
            {
                if (!this.mergeItemStack(slotContents, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }

                return remaining;
            }
            else if(slot.isItemValid(slotContents))
            {
                if (!this.mergeItemStack(slotContents, slotBelt.slotNumber,  slotBelt.slotNumber + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
        }

        return super.transferStackInSlot(playerIn, index);
    }
}