package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ToolBeltItem;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;

public class BeltFinderBeltSlot extends BeltFinder
{
    public static final String FINDER_ID = "belt_slot";

    @CapabilityInject(BeltExtensionSlot.class)
    public static void initBaubles(Capability<?> cap)
    {
        BeltFinder.addFinder(new BeltFinderBeltSlot());
    }

    @Override
    public String getName()
    {
        return FINDER_ID;
    }

    @Override
    public Optional<? extends BeltGetter> findStack(PlayerEntity player)
    {
        //noinspection NullableProblems
        return BeltExtensionSlot.get(player)
                .map((theCap) -> theCap.getSlots().stream()
                        .filter(slot -> slot.getContents().getItem() instanceof ToolBeltItem)
                        .map(ExtensionSlotBeltGetter::new)
                        .findFirst())
                .orElseGet(Optional::empty);
    }

    @Override
    public void setToSlot(LivingEntity player, int slotNumber, ItemStack stack)
    {
        BeltExtensionSlot.get(player).ifPresent(slot -> slot.getBelt().setContents(stack));
    }

    private static class ExtensionSlotBeltGetter implements BeltGetter
    {
        private final IExtensionSlot slot;

        private ExtensionSlotBeltGetter(IExtensionSlot slot)
        {
            this.slot = slot;
        }

        @Override
        public ItemStack getBelt()
        {
            return slot.getContents();
        }

        @Override
        public void syncToClients()
        {
            LivingEntity thePlayer = slot.getContainer().getOwner();
            if (thePlayer.world.isRemote)
                return;
            BeltContentsChange message = new BeltContentsChange(thePlayer, FINDER_ID, 0, slot.getContents());
            ToolBelt.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> thePlayer), message);
        }
    }
}
