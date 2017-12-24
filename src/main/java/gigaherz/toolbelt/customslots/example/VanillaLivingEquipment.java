package gigaherz.toolbelt.customslots.example;

import com.google.common.collect.ImmutableList;
import gigaherz.toolbelt.customslots.IExtensionContainer;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class VanillaLivingEquipment implements IExtensionContainer
{
    public static final ResourceLocation HEAD = new ResourceLocation("minecraft", EntityEquipmentSlot.HEAD.getName());
    public static final ResourceLocation CHEST = new ResourceLocation("minecraft", EntityEquipmentSlot.CHEST.getName());
    public static final ResourceLocation LEGS = new ResourceLocation("minecraft", EntityEquipmentSlot.LEGS.getName());
    public static final ResourceLocation FEET = new ResourceLocation("minecraft", EntityEquipmentSlot.FEET.getName());
    public static final ResourceLocation OFFHAND = new ResourceLocation("minecraft", EntityEquipmentSlot.OFFHAND.getName());
    public static final ResourceLocation MAINHAND = new ResourceLocation("minecraft", EntityEquipmentSlot.MAINHAND.getName());

    private final EntityLivingBase owner;
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(
            new Slot(HEAD, EntityEquipmentSlot.HEAD),
            new Slot(CHEST, EntityEquipmentSlot.CHEST),
            new Slot(LEGS, EntityEquipmentSlot.LEGS),
            new Slot(FEET, EntityEquipmentSlot.FEET),
            new Slot(OFFHAND, EntityEquipmentSlot.OFFHAND),
            new Slot(MAINHAND, EntityEquipmentSlot.MAINHAND)
    );

    public VanillaLivingEquipment(EntityLivingBase owner)
    {
        this.owner = owner;
    }

    @Nonnull
    @Override
    public ImmutableList<IExtensionSlot> getSlots()
    {
        return slots;
    }

    @Nonnull
    @Override
    public EntityLivingBase getOwner()
    {
        return owner;
    }

    private class Slot implements IExtensionSlot
    {
        private final ResourceLocation id;
        private final EntityEquipmentSlot slot;

        private Slot(ResourceLocation id, EntityEquipmentSlot slot)
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
         *
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
        public boolean canEquip(@Nonnull ItemStack stack)
        {
            if (stack.getItem().isValidArmor(stack, slot, owner))
                return true;
            return IExtensionSlot.super.canEquip(stack);
        }
    }
}
