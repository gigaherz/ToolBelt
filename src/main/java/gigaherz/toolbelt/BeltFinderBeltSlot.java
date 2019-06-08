package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class BeltFinderBeltSlot extends BeltFinder
{
    @CapabilityInject(ExtensionSlotBelt.class)
    public static void initBaubles(Capability cap)
    {
        BeltFinder.instances.add(new BeltFinderBeltSlot());
    }

    @Nullable
    public BeltGetter findStack(PlayerEntity player)
    {
        ExtensionSlotBelt baubles = ExtensionSlotBelt.get(player);
        for (IExtensionSlot slot : baubles.getSlots())
        {
            ItemStack inSlot = slot.getContents();
            if (inSlot.getCount() > 0)
            {
                if (inSlot.getItem() instanceof ItemToolBelt)
                {
                    return new ExtensionSlotBeltGetter(slot);
                }
            }
        }

        return null;
    }

    @Override
    public void setToBeltSlot(LivingEntity player, ItemStack stack)
    {
        ExtensionSlotBelt baubles = ExtensionSlotBelt.get(player);
        baubles.getBelt().setContents(stack);
    }

    private class ExtensionSlotBeltGetter implements BeltGetter
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
            BeltContentsChange message = new BeltContentsChange(thePlayer, BeltContentsChange.ContainingInventory.BELT_SLOT, 0, slot.getContents());
            ToolBelt.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> thePlayer), message);
        }
    }
}
