package gigaherz.toolbelt.belt;

import gigaherz.toolbelt.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class ToolBeltInventory implements IItemHandlerModifiable
{
    private final NBTTagCompound nbt;

    ToolBeltInventory(ItemStack itemStack)
    {
        NBTTagCompound tag;
        tag = itemStack.getTagCompound();
        if (tag == null)
            itemStack.setTagCompound(tag = new NBTTagCompound());
        nbt = tag;
    }

    // Ensure that the serialization is always compatible, even if it were to change upstream
    @Override
    public int getSlots()
    {
        return nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            if (itemTags.getInteger("Slot") != slot)
                continue;

            return new ItemStack(itemTags);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);

        NBTTagCompound itemTag = null;
        boolean hasStack = stack.getCount() > 0;
        if (hasStack)
        {
            itemTag = new NBTTagCompound();
            itemTag.setInteger("Slot", slot);
            stack.writeToNBT(itemTag);
        }

        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound existing = tagList.getCompoundTagAt(i);
            if (existing.getInteger("Slot") != slot)
                continue;

            if (hasStack)
                tagList.set(i, itemTag);
            else
                tagList.removeTag(i);
            return;
        }

        if (hasStack)
            tagList.appendTag(itemTag);

        nbt.setTag("Items", tagList);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!Config.isItemStackAllowed(stack))
            return stack;

        if (stack.getCount() <= 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = getStackInSlot(slot);

        int limit = stack.getMaxStackSize();

        if (existing.getCount() > 0)
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate)
        {
            if (existing.getCount() <= 0)
            {
                existing = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
            }
            else
            {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            setStackInSlot(slot, existing);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = getStackInSlot(slot);

        if (existing.getCount() <= 0)
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract)
        {
            if (!simulate)
            {
                setStackInSlot(slot, ItemStack.EMPTY);
            }
            return existing;
        }
        else
        {
            if (!simulate)
            {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    private void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
    }
}
