package dev.gigaherz.toolbelt.belt;

import dev.gigaherz.toolbelt.ConfigData;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class ToolBeltInventory implements IItemHandlerModifiable
{
    private final ItemStack itemStack;

    public ToolBeltInventory(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    private CompoundTag getTag()
    {
        CompoundTag tag;
        tag = itemStack.getTag();
        if (tag == null)
            itemStack.setTag(tag = new CompoundTag());
        return tag;
    }

    // Ensure that the serialization is always compatible, even if it were to change upstream
    @Override
    public int getSlots()
    {
        return Mth.clamp(getTag().getInt("Size"), 2, 9);
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);
        ListTag tagList = getTag().getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            if (itemTags.getInt("Slot") != slot)
                continue;

            return ItemStack.of(itemTags);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);

        CompoundTag itemTag = null;
        boolean hasStack = stack.getCount() > 0;
        if (hasStack)
        {
            itemTag = new CompoundTag();
            itemTag.putInt("Slot", slot);
            stack.save(itemTag);
        }

        ListTag tagList = getTag().getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag existing = tagList.getCompound(i);
            if (existing.getInt("Slot") != slot)
                continue;

            if (hasStack)
                tagList.set(i, itemTag);
            else
                tagList.remove(i);
            return;
        }

        if (hasStack)
            tagList.add(itemTag);

        getTag().put("Items", tagList);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!canInsertItem(slot, stack))
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

    protected boolean canInsertItem(int slot, ItemStack stack)
    {
        return ConfigData.isItemStackAllowed(stack);
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

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return false;
    }

    private void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
    }
}
