package gigaherz.toolbelt.slot;

import com.google.common.collect.ImmutableList;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtensionSlotBelt implements IExtensionContainer, INBTSerializable<NBTTagCompound>
{
    ////////////////////////////////////////////////////////////
    // Capability support code
    //

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation("toolbelt", "belt_slot");

    @CapabilityInject(ExtensionSlotBelt.class)
    public static Capability<ExtensionSlotBelt> CAPABILITY = null;

    public static void register()
    {
        // Internal capability, IStorage and default instances are meaningless.
        CapabilityManager.INSTANCE.register(ExtensionSlotBelt.class, new Capability.IStorage<ExtensionSlotBelt>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<ExtensionSlotBelt> capability, ExtensionSlotBelt instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<ExtensionSlotBelt> capability, ExtensionSlotBelt instance, EnumFacing side, NBTBase nbt)
            {

            }
        }, () -> null);

        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    public static ExtensionSlotBelt get(EntityLivingBase player)
    {
        return player.getCapability(CAPABILITY, null);
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
                    final ExtensionSlotBelt extensionContainer = new ExtensionSlotBelt((EntityPlayer) event.getObject());

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

                    @Override
                    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
                    {
                        if (capability == CAPABILITY)
                            return true;
                        return false;
                    }

                    @Nullable
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
                    {
                        if (capability == CAPABILITY)
                            return (T) extensionContainer;
                        return null;
                    }
                });
            }
        }

        @SubscribeEvent
        public void entityTick(TickEvent.PlayerTickEvent event)
        {
            ExtensionSlotBelt instance = get(event.player);
            if (instance == null) return;
            instance.tickAllSlots();
        }
    }

    ////////////////////////////////////////////////////////////
    // Equipment container implementation
    //
    public static final ResourceLocation BELT = new ResourceLocation("examplemod", "belt");

    private final EntityLivingBase owner;
    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private final ExtensionSlotItemHandler belt = new ExtensionSlotItemHandler(this, BELT, inventory, 0);
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(belt);

    private ExtensionSlotBelt(EntityLivingBase owner)
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
    public IExtensionSlot getBelt()
    {
        return belt;
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
