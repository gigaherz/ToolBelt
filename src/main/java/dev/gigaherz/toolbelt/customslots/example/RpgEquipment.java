package dev.gigaherz.toolbelt.customslots.example;

import com.google.common.collect.ImmutableList;
import dev.gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import dev.gigaherz.toolbelt.customslots.IExtensionContainer;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RpgEquipment implements IExtensionContainer, INBTSerializable<CompoundTag>
{
    ////////////////////////////////////////////////////////////
    // Capability support code
    //

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation("examplemod", "rpg_inventory");

    public static Capability<RpgEquipment> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(RpgEquipment.class);

        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    public static RpgEquipment get(Player player)
    {
        return player.getCapability(CAPABILITY, null).orElseThrow(() -> new RuntimeException("Capability not attached!"));
    }

    static class EventHandlers
    {
        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof Player)
            {
                event.addCapability(CAPABILITY_ID, new ICapabilitySerializable<CompoundTag>()
                {
                    final RpgEquipment extensionContainer = new RpgEquipment((Player) event.getObject());

                    final LazyOptional<RpgEquipment> extensionContainerInstance = LazyOptional.of(() -> extensionContainer);

                    @Override
                    public CompoundTag serializeNBT()
                    {
                        return extensionContainer.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt)
                    {
                        extensionContainer.deserializeNBT(nbt);
                    }

                    @Nullable
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
                    {
                        if (capability == CAPABILITY)
                            return (LazyOptional<T>) extensionContainerInstance;
                        return LazyOptional.empty();
                    }
                });
            }
        }

        @SubscribeEvent
        public void entityTick(TickEvent.PlayerTickEvent event)
        {
            if (event.phase == TickEvent.Phase.END)
            {
                RpgEquipment instance = get(event.player);
                if (instance == null) return;
                instance.tickAllSlots();
            }
        }
    }

    ////////////////////////////////////////////////////////////
    // Equipment container implementation
    //

    public static final ResourceLocation RING = new ResourceLocation("examplemod", "ring");
    public static final ResourceLocation TRINKET = new ResourceLocation("examplemod", "trinket");
    public static final ResourceLocation NECK = new ResourceLocation("examplemod", "neck");
    public static final ResourceLocation BELT = new ResourceLocation("examplemod", "belt");
    public static final ResourceLocation WRISTS = new ResourceLocation("examplemod", "wrists");
    public static final ResourceLocation ANKLES = new ResourceLocation("examplemod", "ankles");

    private final LivingEntity owner;
    private final ItemStackHandler inventory = new ItemStackHandler(8);
    private final ExtensionSlotItemHandler ring1 = new ExtensionSlotItemHandler(this, RING, inventory, 0);
    private final ExtensionSlotItemHandler ring2 = new ExtensionSlotItemHandler(this, RING, inventory, 1);
    private final ExtensionSlotItemHandler trinket1 = new ExtensionSlotItemHandler(this, TRINKET, inventory, 2);
    private final ExtensionSlotItemHandler trinket2 = new ExtensionSlotItemHandler(this, TRINKET, inventory, 3);
    private final ExtensionSlotItemHandler neck = new ExtensionSlotItemHandler(this, NECK, inventory, 4);
    private final ExtensionSlotItemHandler belt = new ExtensionSlotItemHandler(this, BELT, inventory, 5);
    private final ExtensionSlotItemHandler wrists = new ExtensionSlotItemHandler(this, WRISTS, inventory, 6);
    private final ExtensionSlotItemHandler ankles = new ExtensionSlotItemHandler(this, ANKLES, inventory, 7);
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(
            ring1, ring2, trinket1, trinket2,
            neck, belt, wrists, ankles
    );

    private RpgEquipment(LivingEntity owner)
    {
        this.owner = owner;
    }

    @Nonnull
    @Override
    public LivingEntity getOwner()
    {
        return owner;
    }

    @Nonnull
    @Override
    public ImmutableList<IExtensionSlot> getSlots()
    {
        return slots;
    }

    @Override
    public void onContentsChanged(IExtensionSlot slot)
    {

    }

    @Nonnull
    public IExtensionSlot getRing1()
    {
        return ring1;
    }

    @Nonnull
    public IExtensionSlot getRing2()
    {
        return ring2;
    }

    @Nonnull
    public IExtensionSlot getTrinket1()
    {
        return trinket1;
    }

    @Nonnull
    public IExtensionSlot getTrinket2()
    {
        return trinket2;
    }

    @Nonnull
    public IExtensionSlot getNeck()
    {
        return neck;
    }

    @Nonnull
    public IExtensionSlot getBelt()
    {
        return belt;
    }

    @Nonnull
    public IExtensionSlot getWrists()
    {
        return wrists;
    }

    @Nonnull
    public IExtensionSlot getAnkles()
    {
        return ankles;
    }

    private void tickAllSlots()
    {
        for (IExtensionSlot slot : slots)
        {
            ((ExtensionSlotItemHandler) slot).onWornTick();
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        inventory.deserializeNBT(nbt);
    }
}
