package gigaherz.toolbelt.slot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BeltExtensionSlot implements IExtensionContainer, INBTSerializable<CompoundNBT>
{
    ////////////////////////////////////////////////////////////
    // Capability support code
    //
    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation("toolbelt", "belt_slot");

    @CapabilityInject(BeltExtensionSlot.class)
    public static Capability<BeltExtensionSlot> CAPABILITY = null;

    public static void register()
    {
        // Internal capability, IStorage and default instances are meaningless.
        CapabilityManager.INSTANCE.register(BeltExtensionSlot.class, new Capability.IStorage<BeltExtensionSlot>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<BeltExtensionSlot> capability, BeltExtensionSlot instance, Direction side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<BeltExtensionSlot> capability, BeltExtensionSlot instance, Direction side, INBT nbt)
            {

            }
        }, () -> null);

        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    public static LazyOptional<BeltExtensionSlot> get(LivingEntity player)
    {
        return player.getCapability(CAPABILITY, null);
    }

    static class EventHandlers
    {
        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof PlayerEntity)
            {
                event.addCapability(CAPABILITY_ID, new ICapabilitySerializable<CompoundNBT>()
                {
                    final BeltExtensionSlot extensionContainer = new BeltExtensionSlot((PlayerEntity) event.getObject())
                    {
                        @Override
                        public void onContentsChanged(IExtensionSlot slot)
                        {
                            if (!getOwner().world.isRemote)
                                syncTo(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getOwner));
                        }
                    };

                    final LazyOptional<BeltExtensionSlot> extensionContainerInstance = LazyOptional.of(() -> extensionContainer);

                    @Override
                    public CompoundNBT serializeNBT()
                    {
                        return extensionContainer.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundNBT nbt)
                    {
                        extensionContainer.deserializeNBT(nbt);
                    }

                    @Nullable
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
                    {
                        return CAPABILITY.orEmpty(capability, extensionContainerInstance);
                    }
                });
            }
        }

        @SubscribeEvent
        public void joinWorld(PlayerLoggedInEvent event)
        {
            PlayerEntity target = event.getPlayer();
            if (target.world.isRemote)
                return;
            get(target).ifPresent(BeltExtensionSlot::syncToSelf);
        }

        @SubscribeEvent
        public void joinWorld(PlayerChangedDimensionEvent event)
        {
            PlayerEntity target = event.getPlayer();
            if (target.world.isRemote)
                return;
            get(target).ifPresent(BeltExtensionSlot::syncToSelf);
        }

        @SubscribeEvent
        public void track(PlayerEvent.StartTracking event)
        {
            Entity target = event.getTarget();
            if (target.world.isRemote)
                return;
            if (target instanceof PlayerEntity)
            {
                get((LivingEntity) target).ifPresent(BeltExtensionSlot::syncToSelf);
            }
        }

        @SubscribeEvent
        public void entityTick(TickEvent.PlayerTickEvent event)
        {
            get(event.player).ifPresent(BeltExtensionSlot::tickAllSlots);
        }

        @SubscribeEvent
        public void playerDeath(LivingDropsEvent event)
        {
            if (event.getEntityLiving() instanceof PlayerEntity)
            {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                get(player).ifPresent((instance) -> {
                    if (!player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !player.isSpectator())
                    {
                        event.getDrops().add(player.dropItem(instance.getBelt().getContents(), true, false));
                    }
                });
            }
        }

        @SubscribeEvent
        public void playerClone(PlayerEvent.Clone event)
        {
            PlayerEntity oldPlayer = event.getOriginal();
            oldPlayer.revive();

            PlayerEntity newPlayer = event.getEntityPlayer();
            get(oldPlayer).ifPresent((oldBelt) -> {
                BeltExtensionSlot newBelt = get(newPlayer).orElse(null);
                ItemStack item = oldBelt.getBelt().getContents();
                if (newBelt == null) {
                    newPlayer.world.addEntity(oldPlayer.dropItem(item, true, false));
                    return;
                }
                if (!event.isWasDeath() || newPlayer.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator())
                {
                    newBelt.getBelt().setContents(oldBelt.getBelt().getContents());
                }
            });
        }
    }

    private void syncToSelf()
    {
        syncTo((PlayerEntity) owner);
    }

    protected void syncTo(PlayerEntity target)
    {
        SyncBeltSlotContents message = new SyncBeltSlotContents((PlayerEntity) owner, this);
        ToolBelt.channel.sendTo(message, ((ServerPlayerEntity) target).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    protected void syncTo(PacketDistributor.PacketTarget target)
    {
        SyncBeltSlotContents message = new SyncBeltSlotContents((PlayerEntity) owner, this);
        ToolBelt.channel.send(target, message);
    }

    ////////////////////////////////////////////////////////////
    // Equipment container implementation
    //
    public static final ResourceLocation BELT = new ResourceLocation("examplemod", "belt");

    private final LivingEntity owner;
    private final ItemStackHandler inventory = new ItemStackHandler(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            belt.onContentsChanged();
        }
    };
    private final ExtensionSlotItemHandler belt = new ExtensionSlotItemHandler(this, BELT, inventory, 0);
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(belt);

    private BeltExtensionSlot(LivingEntity owner)
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
    public CompoundNBT serializeNBT()
    {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        inventory.deserializeNBT(nbt);
    }
}
