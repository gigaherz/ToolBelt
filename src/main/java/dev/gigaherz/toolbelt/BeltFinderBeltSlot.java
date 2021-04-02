package dev.gigaherz.toolbelt;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import dev.gigaherz.toolbelt.integration.CosmeticArmorIntegration;
import dev.gigaherz.toolbelt.network.BeltContentsChange;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

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
        return BeltExtensionSlot.get(player)
                .resolve()
                .flatMap(ext -> ext.getSlots().stream()
                        .filter(slot -> slot.getContents().getItem() instanceof ToolBeltItem)
                        .map(slot -> new ExtensionSlotBeltGetter(player, slot))
                        .findFirst());
    }

    @Override
    public void setToSlot(LivingEntity player, int slotNumber, ItemStack stack)
    {
        BeltExtensionSlot.get(player).ifPresent(slot -> slot.getBelt().setContents(stack));
    }

    private static class ExtensionSlotBeltGetter implements BeltGetter
    {
        private PlayerEntity player;
        private final IExtensionSlot slot;

        private ExtensionSlotBeltGetter(PlayerEntity player, IExtensionSlot slot)
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
        public boolean isHidden()
        {
            if (ModList.get().isLoaded("cosmeticarmorreworked"))
            {
                if (CosmeticArmorIntegration.isHidden(player, ToolBelt.MODID, "belt#0"))
                    return true;
            }

            return false;
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
