package dev.gigaherz.toolbelt;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import dev.gigaherz.toolbelt.network.BeltContentsChange;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public class BeltFinderBeltSlot extends BeltFinder
{
    public static final String FINDER_ID = "belt_slot";

    public static Capability<BeltExtensionSlot> BELT_EXTENSION_SLOT_CAPABILITY
            = CapabilityManager.get(new CapabilityToken<>(){});

    public static void initBaubles()
    {
        BeltFinder.addFinder(new BeltFinderBeltSlot());
    }

    @Override
    protected Optional<BeltGetter> getSlotFromId(Player player, JsonElement packetData)
    {
        return BeltExtensionSlot.get(player)
                .resolve()
                .map(BeltExtensionSlot::getSlots)
                .map(slots -> slots.get(packetData.getAsInt()))
                .map(slot -> new ExtensionSlotBeltGetter(player,  slot));
    }

    @Override
    public String getName()
    {
        return FINDER_ID;
    }

    @Override
    public Optional<? extends BeltGetter> findStack(LivingEntity player, boolean allowCosmetic)
    {
        return BeltExtensionSlot.get(player)
                .resolve()
                .flatMap(ext -> ext.getSlots().stream()
                        .filter(slot -> slot.getContents().getItem() instanceof ToolBeltItem)
                        .map(slot -> new ExtensionSlotBeltGetter(player, slot))
                        .findFirst());
    }

    private static class ExtensionSlotBeltGetter implements BeltGetter
    {
        private final LivingEntity player;
        private final IExtensionSlot slot;

        private ExtensionSlotBeltGetter(LivingEntity player, IExtensionSlot slot)
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
            LivingEntity thePlayer = slot.getContainer().getOwner();
            if (thePlayer.level.isClientSide)
                return;
            BeltContentsChange message = new BeltContentsChange(thePlayer, FINDER_ID, new JsonPrimitive(0), slot.getContents());
            ToolBelt.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> thePlayer), message);
        }
    }
}
