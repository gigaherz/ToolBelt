package gigaherz.toolbelt.common;

import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotBelt extends SlotItemHandler
{
    public SlotBelt(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (!Config.isItemStackAllowed(stack))
            return false;
        return super.isItemValid(stack);
    }
}
