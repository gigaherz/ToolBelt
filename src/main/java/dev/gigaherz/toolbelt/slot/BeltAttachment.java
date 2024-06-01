package dev.gigaherz.toolbelt.slot;

import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.network.SyncBeltSlotContents;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class BeltAttachment implements INBTSerializable<CompoundTag>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean ENABLE_DEBUG_LOGGING = "true".equals(System.getProperty("toolbelt.debug", FMLEnvironment.production ? "false" : "true"));

    private static void printDebugLog(String message, Object... params)
    {
        if (ENABLE_DEBUG_LOGGING)
        {
            LOGGER.info(message, params);
        }
    }

    ////////////////////////////////////////////////////////////
    // Capability support code
    //

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ToolBelt.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BeltAttachment>> BELT_ATTACHMENT = ATTACHMENT_TYPES.register("belt", () ->
            AttachmentType
                    .serializable(BeltAttachment::new)
                    .build()
            );

    public static void register(IEventBus modEventBus)
    {
        ATTACHMENT_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.register(new EventHandlers());
    }

    public static BeltAttachment get(LivingEntity player)
    {
        return player.getData(BELT_ATTACHMENT);
    }

    private static class EventHandlers
    {
        @SubscribeEvent
        public void attachCapabilities(EntityJoinLevelEvent event)
        {
        }

        @SubscribeEvent
        public void joinWorld(PlayerEvent.PlayerLoggedInEvent event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            Player target = event.getEntity();
            if (target.level().isClientSide)
                return;
            get(target).syncToSelf();
        }

        @SubscribeEvent
        public void joinWorld(PlayerEvent.PlayerChangedDimensionEvent event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            Player target = event.getEntity();
            if (target.level().isClientSide)
                return;
            get(target).syncToSelf();
        }

        @SubscribeEvent
        public void track(PlayerEvent.StartTracking event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            Entity target = event.getTarget();
            if (target.level().isClientSide)
                return;
            if (target instanceof Player)
            {
                get((LivingEntity) target).syncToSelf();
            }
        }

        @SubscribeEvent
        public void entityTick(TickEvent.PlayerTickEvent event)
        {
            if (event.phase != TickEvent.Phase.END)
                return;

            if (ConfigData.customBeltSlotEnabled)
                get(event.player).onWornTick();
            else
                get(event.player).dropContents();
        }

        @SubscribeEvent
        public void playerDeath(LivingDropsEvent event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            LivingEntity entity = event.getEntity();

            var attachment = get(entity);

            ItemStack stack = attachment.getContents();
            if (stack.getCount() > 0)
            {
                printDebugLog("Processing belt slot data for entity death {}({})", entity.getScoreboardName(), entity.getUUID());

                if (EnchantmentHelper.hasVanishingCurse(stack))
                {
                    stack = ItemStack.EMPTY;
                    attachment.setContents(stack);
                }
                else
                {
                    if (entity instanceof Player player)
                    {
                        if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator())
                        {
                            printDebugLog("Entity is player, and keepInventory is not set. Spilling...");
                            Collection<ItemEntity> old = entity.captureDrops(event.getDrops());
                            player.drop(stack, true, false);
                            entity.captureDrops(old);
                            attachment.setContents(ItemStack.EMPTY);
                        }
                    }
                    else
                    {
                        entity.spawnAtLocation(stack);
                        attachment.setContents(ItemStack.EMPTY);
                    }
                }
            }
        }

        @SubscribeEvent
        public void playerClone(PlayerEvent.Clone event)
        {
            if (!ConfigData.customBeltSlotEnabled)
                return;
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            printDebugLog("Processing respawn for entity {}({})", newPlayer.getScoreboardName(), newPlayer.getUUID());
            var oldBelt = get(oldPlayer);
            printDebugLog("Old entity has data, copying...");
            ItemStack stack = oldBelt.getContents();
            var newBelt = get(newPlayer);
            newBelt.setContents(stack);
        }
    }

    private void dropContents()
    {
        ItemStack stack = getContents();
        if (stack.getCount() > 0)
        {
            printDebugLog("Entity {}({}) has item in the belt slot, but the belt is disabled. Dropping to the ground.", owner.getScoreboardName(), owner.getUUID());
            if (owner instanceof Player)
                ItemHandlerHelper.giveItemToPlayer((Player) owner, stack);
            else
                owner.spawnAtLocation(stack, 0.1f);
            setContents(ItemStack.EMPTY);
        }
    }

    private void syncToSelf()
    {
        syncTo((Player) owner);
    }

    protected void syncTo(Player target)
    {
        PacketDistributor.PLAYER.with((ServerPlayer) target).send(new SyncBeltSlotContents((Player) owner, this));
    }

    protected void syncTo(PacketDistributor.PacketTarget target)
    {
        target.send(new SyncBeltSlotContents((Player) owner, this));
    }

    private final LivingEntity owner;
    private final ItemStackHandler inventory = new ItemStackHandler(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            BeltAttachment.this.onContentsChanged();
        }
    };

    public BeltAttachment(IAttachmentHolder holder)
    {
        this.owner = (LivingEntity)holder;
    }

    @Nullable
    public LivingEntity getOwner()
    {
        return owner;
    }

    public void onContentsChanged()
    {
        if (!ConfigData.customBeltSlotEnabled)
            return;
        if (getOwner() != null && !getOwner().level().isClientSide)
            syncTo(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(getOwner()));
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

    /**
     * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
     */
    @Nonnull
    public ItemStack getContents()
    {
        return inventory.getStackInSlot(0);
    }

    public void setContents(@Nonnull ItemStack stack)
    {
        ItemStack oldStack = getContents();
        if (oldStack == stack) return;
        if (!oldStack.isEmpty())
            notifyUnequip(oldStack);
        inventory.setStackInSlot(0, stack);
        if (!stack.isEmpty())
            notifyEquip(stack);
    }

    private void notifyEquip(ItemStack stack)
    {
        var extItem = stack.getCapability(IBeltSlotItem.CAPABILITY);
        if (extItem != null) {
            extItem.onEquipped(stack, this);
        }
    }

    private void notifyUnequip(ItemStack stack)
    {
        var extItem = stack.getCapability(IBeltSlotItem.CAPABILITY);
        if (extItem != null) {
            extItem.onUnequipped(stack, this);
        }
    }

    private void onWornTick()
    {
        ItemStack stack = getContents();
        if (stack.isEmpty())
            return;
        var extItem = stack.getCapability(IBeltSlotItem.CAPABILITY);
        if (extItem != null) {
            extItem.onWornTick(stack, this);
        }
    }

    public boolean canEquip(ItemStack stack)
    {
        if (stack.isEmpty())
            return false;
        var extItem = stack.getCapability(IBeltSlotItem.CAPABILITY);
        if (extItem != null) {
            return extItem.canEquip(stack, this);
        }
        return false;
    }

    public boolean canUnequip()
    {
        ItemStack stack = getContents();
        if (stack.isEmpty())
            return false;
        var extItem = stack.getCapability(IBeltSlotItem.CAPABILITY);
        if (extItem != null) {
            return extItem.canEquip(stack, this);
        }
        return true;
    }
}
