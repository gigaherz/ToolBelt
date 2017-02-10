package gigaherz.toolbelt.belt;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;

public class ToolBeltInventory extends ItemStackHandler
{
    public ToolBeltInventory()
    {
        super(2);
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
    }
}
