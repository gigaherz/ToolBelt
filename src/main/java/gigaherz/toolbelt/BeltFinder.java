package gigaherz.toolbelt;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;

public abstract class BeltFinder
{
    public static NonNullList<BeltFinder> instances = NonNullList.create();

    @Nullable
    public abstract BeltGetter findStack(PlayerEntity player);

    @Nullable
    public static BeltGetter findBelt(PlayerEntity player)
    {
        for (int i = instances.size() - 1; i >= 0; i--)
        {
            BeltFinder instance = instances.get(i);
            BeltGetter getter = instance.findStack(player);
            if (getter != null)
                return getter;
        }
        return null;
    }

    public static void sendSync(PlayerEntity player)
    {
        BeltFinder.BeltGetter stack = findBelt(player);
        if (stack != null)
        {
            stack.syncToClients();
        }
    }

    public void setToBaubles(PlayerEntity player, int slot, ItemStack stack)
    {
    }

    public void setToBeltSlot(LivingEntity player, ItemStack stack)
    {
    }

    public interface BeltGetter
    {
        ItemStack getBelt();

        void syncToClients();
    }
}
