package gigaherz.toolbelt.customslots.example;

import com.google.common.collect.ImmutableList;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RpgEquipment implements IExtensionContainer, INBTSerializable<NBTTagCompound>
{
    ////////////////////////////////////////////////////////////
    // Capability support code
    //

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation("examplemod", "rpg_inventory");

    @CapabilityInject(RpgEquipment.class)
    public static Capability<RpgEquipment> CAPABILITY = null;

    public static void register()
    {
        // Internal capability, IStorage and default instances are meaningless.
        CapabilityManager.INSTANCE.register(RpgEquipment.class, new Capability.IStorage<RpgEquipment>()
        {
            @Nullable
            @Override
            public INBTBase writeNBT(Capability<RpgEquipment> capability, RpgEquipment instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<RpgEquipment> capability, RpgEquipment instance, EnumFacing side, INBTBase nbt)
            {

            }
        }, () -> null);

        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    public static RpgEquipment get(EntityPlayer player)
    {
        return player.getCapability(CAPABILITY, null).orElseThrow(() -> new RuntimeException("Capability not attached!"));
    }

    static class EventHandlers
    {
        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof EntityPlayer)
            {
                event.addCapability(CAPABILITY_ID, new ICapabilitySerializable<NBTTagCompound>()
                {
                    final RpgEquipment extensionContainer = new RpgEquipment((EntityPlayer) event.getObject());

                    final LazyOptional<RpgEquipment> extensionContainerInstance = LazyOptional.of(() -> extensionContainer);

                    @Override
                    public NBTTagCompound serializeNBT()
                    {
                        return extensionContainer.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(NBTTagCompound nbt)
                    {
                        extensionContainer.deserializeNBT(nbt);
                    }

                    @Nullable
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
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
            RpgEquipment instance = get(event.player);
            if (instance == null) return;
            instance.tickAllSlots();
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

    private final EntityLivingBase owner;
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

    private RpgEquipment(EntityLivingBase owner)
    {
        this.owner = owner;
    }

    @Nonnull
    @Override
    public EntityLivingBase getOwner()
    {
        return owner;
    }

    @Nonnull
    @Override
    public ImmutableList<IExtensionSlot> getSlots()
    {
        return slots;
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
    public NBTTagCompound serializeNBT()
    {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        inventory.deserializeNBT(nbt);
    }
}
