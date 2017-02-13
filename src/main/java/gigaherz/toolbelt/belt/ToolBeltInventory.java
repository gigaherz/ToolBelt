package gigaherz.toolbelt.belt;

import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class ToolBeltInventory extends ItemStackHandler
{
    boolean needsUpdate;
    ItemStack stack;

    public ToolBeltInventory(ItemStack itemStack)
    {
        super(2);
        stack = itemStack;
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
        {
            this.deserializeNBT(nbt);
        }
        ToolBelt.addWeakListener(this);
    }

    // Ensure that the serialization is always compatible, even if it were to change upstream
    private NBTTagCompound writeNBT(NBTTagCompound nbt)
    {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                stacks.get(i).writeToNBT(itemTag);
                nbtTagList.appendTag(itemTag);
            }
        }
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", stacks.size());
        return nbt;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeNBT(nbt);
    }

    // Ensure that the serialization is always compatible, even if it were to change upstream
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, new ItemStack(itemTags));
            }
        }
        onLoad();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!Config.isItemStackAllowed(stack))
            return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public void setSize(int newCount)
    {
        NonNullList<ItemStack> oldStacks = stacks;
        stacks = NonNullList.withSize(newCount, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(oldStacks.size(), stacks.size()); i++)
        { stacks.set(i, oldStacks.get(i)); }
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

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
            nbt = new NBTTagCompound();

        stack.setTagCompound(writeNBT(nbt));
    }
}
