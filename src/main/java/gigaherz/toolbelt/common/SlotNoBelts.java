package gigaherz.toolbelt.common;

import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotNoBelts extends SlotItemHandler
{
    public SlotNoBelts(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (stack != null && stack.getItem() instanceof ItemToolBelt)
            return false;
        return super.isItemValid(stack);
    }
}
