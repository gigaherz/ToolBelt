package dev.gigaherz.toolbelt.customslots.example;

import com.google.common.collect.ImmutableList;
import dev.gigaherz.toolbelt.customslots.IExtensionContainer;
import dev.gigaherz.toolbelt.customslots.IExtensionSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class VanillaLivingEquipment implements IExtensionContainer
{
    public static final ResourceLocation HEAD = new ResourceLocation("minecraft", EquipmentSlot.HEAD.getName());
    public static final ResourceLocation CHEST = new ResourceLocation("minecraft", EquipmentSlot.CHEST.getName());
    public static final ResourceLocation LEGS = new ResourceLocation("minecraft", EquipmentSlot.LEGS.getName());
    public static final ResourceLocation FEET = new ResourceLocation("minecraft", EquipmentSlot.FEET.getName());
    public static final ResourceLocation OFFHAND = new ResourceLocation("minecraft", EquipmentSlot.OFFHAND.getName());
    public static final ResourceLocation MAINHAND = new ResourceLocation("minecraft", EquipmentSlot.MAINHAND.getName());

    private final LivingEntity owner;
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(
            new Slot(HEAD, EquipmentSlot.HEAD),
            new Slot(CHEST, EquipmentSlot.CHEST),
            new Slot(LEGS, EquipmentSlot.LEGS),
            new Slot(FEET, EquipmentSlot.FEET),
            new Slot(OFFHAND, EquipmentSlot.OFFHAND),
            new Slot(MAINHAND, EquipmentSlot.MAINHAND)
    );

    public VanillaLivingEquipment(LivingEntity owner)
    {
        this.owner = owner;
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
    @Override
    public LivingEntity getOwner()
    {
        return owner;
    }

    private class Slot implements IExtensionSlot
    {
        private final ResourceLocation id;
        private final EquipmentSlot slot;

        private Slot(ResourceLocation id, EquipmentSlot slot)
        {
            this.id = id;
            this.slot = slot;
        }

        @Nonnull
        @Override
        public IExtensionContainer getContainer()
        {
            return VanillaLivingEquipment.this;
        }

        @Nonnull
        @Override
        public ResourceLocation getType()
        {
            return id;
        }

        /**
         * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
         */
        @Nonnull
        @Override
        public ItemStack getContents()
        {
            return owner.getItemBySlot(slot);
        }

        @Override
        public void setContents(@Nonnull ItemStack stack)
        {
            owner.setItemSlot(slot, stack);
        }

        @Override
        public void onContentsChanged()
        {
            VanillaLivingEquipment.this.onContentsChanged(this);
        }

        @Override
        public boolean canEquip(@Nonnull ItemStack stack)
        {
            if (stack.getItem().canEquip(stack, slot, owner))
                return true;
            return IExtensionSlot.super.canEquip(stack);
        }
    }
}
