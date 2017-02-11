package gigaherz.toolbelt.belt;

import gigaherz.toolbelt.ToolBelt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;

public class ToolBeltInventory extends ItemStackHandler
{
    boolean needsUpdate;
    ItemStack stack;

    public ToolBeltInventory(ItemStack itemStack)
    {
        super(2);
        stack = itemStack;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            NBTTagCompound nbt = tag.getCompoundTag("Items");
            if (nbt != null)
            {
                this.deserializeNBT(nbt);
            }
        }

        ToolBelt.addWeakListener(this);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!ItemToolBelt.isItemValid(stack))
            return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public void setSize(int newCount)
    {
        stacks = Arrays.copyOf(stacks, newCount);
        needsUpdate = true;
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);
        needsUpdate = true;
    }

    public void update()
    {
        if (!needsUpdate) return;
        needsUpdate = false;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            tag = new NBTTagCompound();
        }

        tag.setTag("Items", serializeNBT());
        stack.setTagCompound(tag);
    }
}
