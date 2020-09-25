package gigaherz.toolbelt.slot;

import com.google.common.collect.ImmutableList;
import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.ExtensionSlotItemHandler;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

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
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<BeltExtensionSlot> capability, BeltExtensionSlot instance, Direction side, INBT nbt)
            {
                instance.deserializeNBT((CompoundNBT) nbt);
            }
        }, () -> {
            throw new RuntimeException("Can not instantiate this way. The capability needs a player as context.");
        });

        MinecraftForge.EVENT_BUS.register(new AttachHandlers());
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
    }

    public static LazyOptional<BeltExtensionSlot> get(LivingEntity player)
    {
        return player.getCapability(CAPABILITY);
    }

    static class AttachHandlers
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
                            if (!ConfigData.customBeltSlotEnabled)
                                return;
                            if (!getOwner().world.isRemote)
                                syncTo(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getOwner));
                        }
                    };

                    final LazyOptional<BeltExtensionSlot> extensionContainerSupplier = LazyOptional.of(() -> extensionContainer);

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

                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
                    {
                        if (CAPABILITY == capability)
                            return extensionContainerSupplier.cast();

                        return LazyOptional.empty();
                    }
                });
            }
        }
    }

    private static class EventHandlers
    {
        @SubscribeEvent
        public void joinWorld(PlayerEvent.PlayerLoggedInEvent event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            PlayerEntity target = event.getPlayer();
            if (target.world.isRemote)
                return;
            get(target).ifPresent(BeltExtensionSlot::syncToSelf);
        }

        @SubscribeEvent
        public void joinWorld(PlayerEvent.PlayerChangedDimensionEvent event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            PlayerEntity target = event.getPlayer();
            if (target.world.isRemote)
                return;
            get(target).ifPresent(BeltExtensionSlot::syncToSelf);
        }

        @SubscribeEvent
        public void track(PlayerEvent.StartTracking event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
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
            if (event.phase != TickEvent.Phase.END)
                return;

            if (ConfigData.customBeltSlotEnabled)
                get(event.player).ifPresent(BeltExtensionSlot::tickAllSlots);
            else
                get(event.player).ifPresent(BeltExtensionSlot::dropContents);
        }

        @SubscribeEvent
        public void playerDeath(LivingDropsEvent event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            if (event.getEntityLiving() instanceof PlayerEntity)
            {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                get(player).ifPresent((instance) -> {
                    IExtensionSlot belt = instance.getBelt();
                    ItemStack stack = belt.getContents();
                    if (EnchantmentHelper.hasVanishingCurse(stack))
                    {
                        stack = ItemStack.EMPTY;
                        belt.setContents(stack);
                    }
                    if (!player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !player.isSpectator())
                    {
                        if (stack.getCount() > 0)
                        {
                            Collection<ItemEntity> old = player.captureDrops(event.getDrops());
                            player.dropItem(stack, true, false);
                            player.captureDrops(old);
                            belt.setContents(ItemStack.EMPTY);
                        }
                    }
                });
            }
        }

        @SubscribeEvent
        public void playerClone(PlayerEvent.Clone event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            PlayerEntity oldPlayer = event.getOriginal();

            // FIXME: workaround for a forge issue that seems to be reappearing too often
            // at this time it's only needed when returning from the end alive
            oldPlayer.revive();

            PlayerEntity newPlayer = event.getPlayer();
            get(oldPlayer).ifPresent((oldBelt) -> {
                ItemStack stack = oldBelt.getBelt().getContents();
                get(newPlayer).map(newBelt -> {
                    // Transfer any remaining item. If it was death and keepInventory was off,
                    // it will have been removed in LivingDropsEvent.
                    newBelt.getBelt().setContents(stack);
                    return Unit.INSTANCE;
                }).orElseGet(() -> {
                    if (stack.getCount() > 0)
                    {
                        oldPlayer.dropItem(stack, true, false);
                    }
                    return Unit.INSTANCE;
                });
            });
        }
    }

    private void dropContents()
    {
        for (IExtensionSlot slot : slots)
        {
            ItemStack stack = slot.getContents();
            if (stack.getCount() > 0)
            {
                if (owner instanceof PlayerEntity)
                    ItemHandlerHelper.giveItemToPlayer((PlayerEntity) owner, stack);
                else
                    owner.entityDropItem(stack, 0.1f);
                slot.setContents(ItemStack.EMPTY);
            }
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
