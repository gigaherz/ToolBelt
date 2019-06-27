package gigaherz.toolbelt.slot;

import com.google.common.collect.ImmutableList;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ExtensionSlotBelt> capability, ExtensionSlotBelt instance, EnumFacing side, NBTBase nbt)
            {
                instance.deserializeNBT((NBTTagCompound)nbt);
            }
        }, () -> { throw new UnsupportedOperationException("Cannot instantiate extension slots without a player, use the class constructor."); });

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
                    final ExtensionSlotBelt extensionContainer = new ExtensionSlotBelt((EntityPlayer) event.getObject())
                    {
                        @Override
                        public void onContentsChanged(IExtensionSlot slot)
                        {
                            if (!getOwner().world.isRemote)
                                syncToSelfAndTracking();
                        }
                    };

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
        public void joinWorld(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event)
        {
            EntityPlayer target = event.player;
            if (target.world.isRemote)
                return;
            ExtensionSlotBelt instance = get(target);
            instance.syncToSelf();
        }

        @SubscribeEvent
        public void joinWorld(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event)
        {
            EntityPlayer target = event.player;
            if (target.world.isRemote)
                return;
            ExtensionSlotBelt instance = get(target);
            instance.syncToSelf();
        }

        @SubscribeEvent
        public void track(PlayerEvent.StartTracking event)
        {
            Entity target = event.getTarget();
            if (target.world.isRemote)
                return;
            if (target instanceof EntityPlayer)
            {
                ExtensionSlotBelt instance = get((EntityLivingBase) target);
                instance.syncTo(event.getEntityPlayer());
            }
        }

        @SubscribeEvent
        public void entityTick(TickEvent.PlayerTickEvent event)
        {
            ExtensionSlotBelt instance = get(event.player);
            if (instance == null) return;
            instance.tickAllSlots();
        }

        @SubscribeEvent
        public void playerDeath(PlayerDropsEvent event)
        {
            EntityPlayer player = event.getEntityPlayer();
            ExtensionSlotBelt instance = get(player);
            if (instance == null) return;
            if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator())
            {
                event.getDrops().add(player.dropItem(instance.getBelt().getContents(), true, false));
            }
        }

        @SubscribeEvent
        public void playerClone(PlayerEvent.Clone event)
        {
            EntityPlayer oldPlayer = event.getOriginal();
            EntityPlayer newPlayer = event.getEntityPlayer();
            ExtensionSlotBelt oldBelt = get(oldPlayer);
            ExtensionSlotBelt newBelt = get(newPlayer);
            if (oldBelt == null) return;
            ItemStack item = oldBelt.getBelt().getContents();
            if (newBelt == null) {
                newPlayer.world.spawnEntity(oldPlayer.dropItem(item, true, false));
                return;
            }
            if (!event.isWasDeath() || newPlayer.world.getGameRules().getBoolean("keepInventory") || oldPlayer.isSpectator())
            {
                newBelt.getBelt().setContents(oldBelt.getBelt().getContents());
            }
        }
    }

    private void syncToSelf()
    {
        syncTo((EntityPlayerMP)owner);
    }

    protected void syncTo(EntityPlayer target)
    {
        SyncBeltSlotContents message = new SyncBeltSlotContents((EntityPlayer) owner, this);
        ToolBelt.channel.sendTo(message, (EntityPlayerMP) target);
    }

    protected void syncToSelfAndTracking()
    {
        SyncBeltSlotContents message = new SyncBeltSlotContents((EntityPlayer) owner, this);
        ToolBelt.channel.sendToAllTracking(message, owner);
        ToolBelt.channel.sendTo(message, (EntityPlayerMP)owner);
    }

    ////////////////////////////////////////////////////////////
    // Equipment container implementation
    //
    public static final ResourceLocation BELT = new ResourceLocation("examplemod", "belt");

    private final EntityLivingBase owner;
    private final ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            belt.onContentsChanged();
        }
    };;
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

    @Override
    public void onContentsChanged(IExtensionSlot slot)
    {

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

    public void setAll(NonNullList<ItemStack> stacks)
    {
        List<IExtensionSlot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++)
        {
            slots.get(i).setContents(stacks.get(i));
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
