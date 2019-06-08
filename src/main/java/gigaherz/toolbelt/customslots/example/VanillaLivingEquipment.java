package gigaherz.toolbelt.customslots.example;

import com.google.common.collect.ImmutableList;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class VanillaLivingEquipment implements IExtensionContainer
{
    public static final ResourceLocation HEAD = new ResourceLocation("minecraft", EquipmentSlotType.HEAD.getName());
    public static final ResourceLocation CHEST = new ResourceLocation("minecraft", EquipmentSlotType.CHEST.getName());
    public static final ResourceLocation LEGS = new ResourceLocation("minecraft", EquipmentSlotType.LEGS.getName());
    public static final ResourceLocation FEET = new ResourceLocation("minecraft", EquipmentSlotType.FEET.getName());
    public static final ResourceLocation OFFHAND = new ResourceLocation("minecraft", EquipmentSlotType.OFFHAND.getName());
    public static final ResourceLocation MAINHAND = new ResourceLocation("minecraft", EquipmentSlotType.MAINHAND.getName());

    private final LivingEntity owner;
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(
            new Slot(HEAD, EquipmentSlotType.HEAD),
            new Slot(CHEST, EquipmentSlotType.CHEST),
            new Slot(LEGS, EquipmentSlotType.LEGS),
            new Slot(FEET, EquipmentSlotType.FEET),
            new Slot(OFFHAND, EquipmentSlotType.OFFHAND),
            new Slot(MAINHAND, EquipmentSlotType.MAINHAND)
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
        private final EquipmentSlotType slot;

        private Slot(ResourceLocation id, EquipmentSlotType slot)
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
            return owner.getItemStackFromSlot(slot);
        }

        @Override
        public void setContents(@Nonnull ItemStack stack)
        {
            owner.setItemStackToSlot(slot, stack);
        }

        @Override
        public void onContentsChanged()
        {

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
