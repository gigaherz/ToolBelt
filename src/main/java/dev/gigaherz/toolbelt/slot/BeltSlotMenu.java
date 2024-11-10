package dev.gigaherz.toolbelt.slot;

import com.mojang.datafixers.util.Pair;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.network.ContainerSlotsHack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class BeltSlotMenu extends AbstractCraftingMenu
{
    private final BeltSlot slotBelt;

    private final CraftingContainer craftingInventory = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer craftResultInventory = new ResultContainer();
    private final Player owner;

    public BeltSlotMenu(int id, Inventory playerInventory)
    {
        super(ToolBelt.BELT_SLOT_MENU.get(), id, 2, 2);
        this.owner = playerInventory.player;

        this.addResultSlot(owner, 154, 28);
        this.addCraftingGridSlots(98, 18);

        for (int i = 0; i < 4; i++)
        {
            EquipmentSlot equipmentslot = InventoryMenu.SLOT_IDS[i];
            ResourceLocation resourcelocation = InventoryMenu.TEXTURE_EMPTY_SLOTS.get(equipmentslot);
            this.addSlot(new ArmorSlot(playerInventory, owner, equipmentslot, 39 - i, 8, 8 + i * 18, resourcelocation));
        }

        this.addStandardInventorySlots(playerInventory, 8, 84);
        this.addSlot(new Slot(playerInventory, 40, 77, 62)
        {
            @Override
            public void setByPlayer(ItemStack p_270969_, ItemStack p_299918_)
            {
                owner.onEquipItem(EquipmentSlot.OFFHAND, p_299918_, p_270969_);
                super.setByPlayer(p_270969_, p_299918_);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
            {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(slotBelt = new BeltSlot(BeltAttachment.get(playerInventory.player), 77, 44));

        if (playerInventory.player.level().isClientSide)
        {
            PacketDistributor.sendToServer(ContainerSlotsHack.INSTANCE);
        }
    }

    public static boolean isHotbarSlot(int index)
    {
        return index >= 36 && index < 45 || index == 45;
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    @Override
    public void slotsChanged(Container inventory)
    {
        if (this.owner != null && this.owner.level() instanceof ServerLevel serverLevel)
        {
            Bridge.slotChangedCraftingGridAccessor(this, serverLevel, this.owner, this.craftingInventory, this.craftResultInventory, null);
        }
    }

    private static class Bridge extends CraftingMenu
    {
        private Bridge(int pContainerId, Inventory pPlayerInventory)
        {
            super(pContainerId, pPlayerInventory);
            throw new IllegalStateException("Not instantiable.");
        }

        public static void slotChangedCraftingGridAccessor(AbstractContainerMenu container, ServerLevel level, Player player, CraftingContainer craftingInventory, ResultContainer craftResultInventory, RecipeHolder<CraftingRecipe> recipeHolder)
        {
            CraftingMenu.slotChangedCraftingGrid(container, level, player, craftingInventory, craftResultInventory, recipeHolder);
        }
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void removed(Player player)
    {
        super.removed(player);
        this.resultSlots.clearContent();
        if (!player.level().isClientSide)
        {
            this.clearContainer(player, this.craftSlots);
        }
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack remaining = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem())
        {
            ItemStack slotContents = slot.getItem();
            remaining = slotContents.copy();
            EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(remaining);

            if (index == slotBelt.index) // allow removing belts from the belt slot
            {
                if (!this.moveItemStackTo(slotContents, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            if (index == 0)
            {
                if (!this.moveItemStackTo(remaining, 9, 45, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(remaining, remaining);
            }
            else if (index >= 1 && index < 5)
            {
                if (!this.moveItemStackTo(remaining, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 5 && index < 9)
            {
                if (!this.moveItemStackTo(remaining, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(8 - equipmentslot.getIndex()).hasItem())
            {
                int i = 8 - equipmentslot.getIndex();
                if (!this.moveItemStackTo(remaining, i, i + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem())
            {
                if (!this.moveItemStackTo(remaining, 45, 46, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (slotBelt.mayPlace(slotContents) && !slotBelt.hasItem()) // try to place belts in the belt slot
            {
                if (!this.moveItemStackTo(slotContents, slotBelt.index, slotBelt.index + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 9 && index < 36)
            {
                if (!this.moveItemStackTo(remaining, 36, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 36 && index < 45)
            {
                if (!this.moveItemStackTo(remaining, 9, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(remaining, 9, 45, false))
            {
                return ItemStack.EMPTY;
            }

            if (remaining.isEmpty())
            {
                slot.setByPlayer(ItemStack.EMPTY, remaining);
            }
            else
            {
                slot.setChanged();
            }

            if (remaining.getCount() == remaining.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, remaining);
            if (index == 0)
            {
                player.drop(remaining, false);
            }
        }

        return remaining;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is null for the initial slot that was double-clicked.
     */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot)
    {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public Slot getResultSlot()
    {
        return this.slots.get(0);
    }

    @Override
    public List<Slot> getInputGridSlots()
    {
        return this.slots.subList(1, 5);
    }

    public CraftingContainer getCraftSlots()
    {
        return this.craftSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType()
    {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner()
    {
        return this.owner;
    }
}