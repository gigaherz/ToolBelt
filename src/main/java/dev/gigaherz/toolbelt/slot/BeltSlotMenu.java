package dev.gigaherz.toolbelt.slot;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.network.ContainerSlotsHack;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class BeltSlotMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe>
{
    private final BeltSlot slotBelt;

    private final CraftingContainer craftingInventory = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer craftResultInventory = new ResultContainer();
    private final Player player;

    public BeltSlotMenu(int id, Inventory playerInventory)
    {
        super(ToolBelt.BELT_SLOT_MENU.get(), id);
        this.player = playerInventory.player;
        this.addSlot(new ResultSlot(playerInventory.player, this.craftingInventory, this.craftResultInventory, 0, 154, 28));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlot(new Slot(this.craftingInventory, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for (int k = 0; k < 4; ++k)
        {
            final EquipmentSlot equipmentslot = InventoryMenu.SLOT_IDS[k];
            this.addSlot(new Slot(playerInventory, 39 - k, 8, 8 + k * 18)
            {
                @Override
                public void setByPlayer(ItemStack p_270969_, ItemStack p_299918_) {
                    player.onEquipItem(equipmentslot, p_299918_, p_270969_);
                    super.setByPlayer(p_270969_, p_299918_);
                }

                @Override
                public int getMaxStackSize()
                {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack)
                {
                    return stack.canEquip(equipmentslot, player);
                }

                @Override
                public boolean mayPickup(Player playerIn)
                {
                    ItemStack itemstack = this.getItem();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && preventArmorChange(itemstack) ? false : super.mayPickup(playerIn);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.TEXTURE_EMPTY_SLOTS.get(equipmentslot));
                }
            });
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        this.addSlot(new Slot(playerInventory, 40, 77, 62) {
            @Override
            public void setByPlayer(ItemStack p_270479_, ItemStack p_299920_) {
                player.onEquipItem(EquipmentSlot.OFFHAND, p_299920_, p_270479_);
                super.setByPlayer(p_270479_, p_299920_);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(slotBelt = new BeltSlot(BeltAttachment.get(playerInventory.player), 77, 44));

        if (playerInventory.player.level().isClientSide)
        {
            PacketDistributor.sendToServer(ContainerSlotsHack.INSTANCE);
        }
    }

    private boolean preventArmorChange(ItemStack stack)
    {
        return EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }

    @Override
    public List<RecipeBookCategories> getRecipeBookCategories()
    {
        return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
    }

    @Override
    public RecipeBookType getRecipeBookType()
    {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public boolean shouldMoveToInventory(int slot)
    {
        return slot != this.getResultSlotIndex();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents itemHelperIn)
    {
        this.craftingInventory.fillStackedContents(itemHelperIn);
    }

    @Override
    public void clearCraftingContent()
    {
        this.craftResultInventory.clearContent();
        this.craftingInventory.clearContent();
    }

    @Override
    public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe)
    {
        return recipe.value().matches(this.craftingInventory.asCraftInput(), this.player.level());
    }

    @Override
    public void slotsChanged(Container inventoryIn)
    {
        Bridge.slotChangedCraftingGridAccessor(this, this.player.level(), this.player, this.craftingInventory, this.craftResultInventory, null);
    }

    private static class Bridge extends CraftingMenu
    {
        private Bridge(int pContainerId, Inventory pPlayerInventory)
        {
            super(pContainerId, pPlayerInventory);
            throw new IllegalStateException("Not instantiable.");
        }

        public static void slotChangedCraftingGridAccessor(AbstractContainerMenu container, Level level, Player player, CraftingContainer craftingInventory, ResultContainer craftResultInventory, RecipeHolder<CraftingRecipe> recipeHolder)
        {
            CraftingMenu.slotChangedCraftingGrid(container, level, player, craftingInventory, craftResultInventory, recipeHolder);
        }
    }

    @Override
    public void removed(Player playerIn)
    {
        super.removed(playerIn);

        this.craftResultInventory.clearContent();

        if (!playerIn.level().isClientSide)
        {
            this.clearContainer(playerIn, this.craftingInventory);
            BeltFinder.sendSync(playerIn);
        }
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return true;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn)
    {
        return slotIn.container != this.craftResultInventory && super.canTakeItemForPickAll(stack, slotIn);
    }

    @Override
    public int getResultSlotIndex()
    {
        return 0;
    }

    @Override
    public int getGridWidth()
    {
        return this.craftingInventory.getWidth();
    }

    @Override
    public int getGridHeight()
    {
        return this.craftingInventory.getHeight();
    }

    @Override
    public int getSize()
    {
        return 5;
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
    }

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
            else if (index == 0)
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
}