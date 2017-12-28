package gigaherz.toolbelt.customslots;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class ExtensionSlotItemHandler implements IExtensionSlot
{
    protected final IExtensionContainer owner;
    protected final ResourceLocation slotType;
    protected final int slot;
    protected final IItemHandlerModifiable inventory;

    public ExtensionSlotItemHandler(IExtensionContainer owner, ResourceLocation slotType, IItemHandlerModifiable inventory, int slot)
    {
        this.owner = owner;
        this.slotType = slotType;
        this.slot = slot;
        this.inventory = inventory;
    }

    @Nonnull
    @Override
    public IExtensionContainer getContainer()
    {
        return owner;
    }

    @Nonnull
    @Override
    public ResourceLocation getType()
    {
        return slotType;
    }

    /**
     * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
     */
    @Nonnull
    @Override
    public ItemStack getContents()
    {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setContents(@Nonnull ItemStack stack)
    {
        ItemStack oldStack = getContents();
        if (oldStack == stack) return;
        if (!oldStack.isEmpty())
            notifyUnequip(oldStack);
        inventory.setStackInSlot(slot, stack);
        if (!stack.isEmpty())
            notifyEquip(stack);
    }

    private void notifyEquip(ItemStack stack)
    {
        IExtensionSlotItem extItem = stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null);
        if (extItem == null)
            return;
        extItem.onEquipped(stack, this);
    }

    private void notifyUnequip(ItemStack stack)
    {
        IExtensionSlotItem extItem = stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null);
        if (extItem == null)
            return;
        extItem.onUnequipped(stack, this);
    }

    public void onWornTick()
    {
        ItemStack stack = getContents();
        if (stack.isEmpty())
            return;
        IExtensionSlotItem extItem = stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null);
        if (extItem == null)
            return;
        extItem.onWornTick(stack, this);
    }
}
