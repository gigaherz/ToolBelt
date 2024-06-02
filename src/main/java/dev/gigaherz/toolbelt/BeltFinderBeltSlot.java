package dev.gigaherz.toolbelt;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.network.BeltContentsChange;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class BeltFinderBeltSlot extends BeltFinder
{
    public static final String FINDER_ID = "belt_slot";

    public static void initBaubles()
    {
        BeltFinder.addFinder(new BeltFinderBeltSlot());
    }

    @Override
    protected Optional<BeltGetter> getSlotFromId(Player player, int slotId)
    {
        return Optional.of(new ExtensionSlotBeltGetter(player, BeltAttachment.get(player)));
    }

    @Override
    public String getName()
    {
        return FINDER_ID;
    }

    @Override
    public Optional<? extends BeltGetter> findStack(LivingEntity player, boolean allowCosmetic)
    {
        var attachment = BeltAttachment.get(player);
        return attachment.getContents().getItem() instanceof ToolBeltItem ?
                        Optional.of(new ExtensionSlotBeltGetter(player, attachment)) : Optional.empty();
    }

    private static class ExtensionSlotBeltGetter implements BeltGetter
    {
        private final LivingEntity player;
        private final BeltAttachment slot;

        private ExtensionSlotBeltGetter(LivingEntity player, BeltAttachment slot)
        {
            this.player = player;
            this.slot = slot;
        }

        @Override
        public ItemStack getBelt()
        {
            return slot.getContents();
        }

        @Override
        public void setBelt(ItemStack stack)
        {
            slot.setContents(stack);
        }

        @Override
        public boolean isHidden()
        {
            return false;
        }

        @Override
        public void syncToClients()
        {
            LivingEntity thePlayer = slot.getOwner();
            if (thePlayer.level().isClientSide)
                return;
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(thePlayer, new BeltContentsChange(thePlayer.getId(), FINDER_ID, 0, slot.getContents()));
        }
    }
}
