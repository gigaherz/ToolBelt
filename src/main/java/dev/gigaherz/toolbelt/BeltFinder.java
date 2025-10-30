package dev.gigaherz.toolbelt;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public abstract class BeltFinder
{
    private static NonNullList<BeltFinder> instances = NonNullList.create();

    public static synchronized void addFinder(BeltFinder finder)
    {
        instances.add(0, finder);
    }

    public static Optional<? extends BeltGetter> findBelt(LivingEntity player)
    {
        return findBelt(player, false);
    }

    public static Optional<? extends BeltGetter> findBelt(LivingEntity player, boolean allowCosmetic)
    {
        return instances.stream()
                .map(f -> f.findStack(player, allowCosmetic))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(Optional::empty);
    }

    public static void sendSync(Player player)
    {
        findBelt(player).ifPresent(BeltGetter::syncToClients);
    }

    public static void setBeltFromPacket(Player player, String where, int slot, ItemStack stack)
    {
        for (BeltFinder finder : instances)
        {
            if (finder.getName().equals(where))
            {
                finder.getSlotFromId(player, slot).ifPresent(getter -> getter.setBelt(stack));
            }
        }
    }

    public abstract String getName();

    public abstract Optional<? extends BeltGetter> findStack(LivingEntity player, boolean allowCosmetic);

    protected Optional<BeltGetter> getSlotFromId(Player player, int slotId)
    {
        return Optional.empty();
    }

    public interface BeltGetter
    {
        ItemStack getBelt();

        default void setBelt(ItemStack stack)
        {
            // Defaults to "do nothing"
        }

        default boolean isHidden()
        {
            return false;
        }

        void syncToClients();
    }
}
