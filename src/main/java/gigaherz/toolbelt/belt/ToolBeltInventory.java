package gigaherz.toolbelt.belt;

import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;

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
    @Nullable
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            if (itemTags.getInteger("Slot") != slot)
                continue;

            return ItemStack.loadItemStackFromNBT(itemTags);
        }

        return null;
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack)
    {
        validateSlotIndex(slot);

        NBTTagCompound itemTag = null;
        if (stack != null)
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

            if (stack != null)
                tagList.set(i, itemTag);
            else
                tagList.removeTag(i);
            return;
        }

        if (stack != null)
            tagList.appendTag(itemTag);

        nbt.setTag("Items", tagList);
    }

    @Override
    @Nullable
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!Config.isItemStackAllowed(stack))
            return stack;

        if (stack == null || stack.stackSize == 0)
            return null;

        validateSlotIndex(slot);

        ItemStack existing = getStackInSlot(slot);

        int limit = getStackLimit(slot, stack);

        if (existing != null)
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.stackSize;
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.stackSize > limit;

        if (!simulate)
        {
            if (existing == null)
            {
                existing = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack;
            }
            else
            {
                existing.stackSize += reachedLimit ? limit : stack.stackSize;
            }
            setStackInSlot(slot, existing);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
    }

    @Override
    @Nullable
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return null;

        validateSlotIndex(slot);

        ItemStack existing = getStackInSlot(slot);

        if (existing == null)
            return null;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.stackSize <= toExtract)
        {
            if (!simulate)
            {
                setStackInSlot(slot, null);
            }
            return existing;
        }
        else
        {
            if (!simulate)
            {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract));
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    //@Override
    public int getStackLimit(int slot, ItemStack stack)
    {
        return stack.getMaxStackSize();
    }

    protected void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
    }
}
