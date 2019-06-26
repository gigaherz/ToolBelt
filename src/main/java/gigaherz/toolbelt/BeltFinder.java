package gigaherz.toolbelt;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public abstract class BeltFinder
{
    private static NonNullList<BeltFinder> instances = NonNullList.create();

    public static void addFinder(BeltFinderBeltSlot finder)
    {
        instances.add(0, finder);
    }

    public static void setToAnyBeltSlot(PlayerEntity player, int slot, ItemStack stack)
    {
        instances.forEach((i) -> i.setToBeltSlot(player, stack));
    }

    public static void setToAnyBaubles(PlayerEntity player, int slot, ItemStack stack)
    {
        instances.forEach((i) -> i.setToBaubles(player, slot, stack));
    }

    public abstract LazyOptional<BeltGetter> findStack(PlayerEntity player);

    public static LazyOptional<BeltGetter> findBelt(PlayerEntity player)
    {
        return instances.stream()
                .map(f -> f.findStack(player))
                .filter(LazyOptional::isPresent)
                .findFirst()
                .orElseGet(LazyOptional::empty);
    }

    public static void sendSync(PlayerEntity player)
    {
        findBelt(player).ifPresent(BeltGetter::syncToClients);
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
