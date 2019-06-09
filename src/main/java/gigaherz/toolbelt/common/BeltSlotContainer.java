package gigaherz.toolbelt.common;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.ExtensionSlotSlot;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.ContainerSlotsHack;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class BeltSlotContainer extends RecipeBookContainer<CraftingInventory>
{
    @ObjectHolder("toolbelt:belt_slot_container")
    public static ContainerType<BeltSlotContainer> TYPE;

    public static final ResourceLocation SLOT_BACKGROUND = ToolBelt.location("textures/gui/empty_belt_slot_background.png");

    private final ExtensionSlotSlot slotBelt;
    private final IExtensionSlot extensionSlot;

    private final CraftingInventory field_75181_e = new CraftingInventory(this, 2, 2);
    private final CraftResultInventory field_75179_f = new CraftResultInventory();
    public final boolean isLocalWorld;
    private final PlayerEntity field_82862_h;

    public BeltSlotContainer(int id, PlayerInventory inventory)
    {
        this(id, inventory, false, inventory.player);
    }

    public BeltSlotContainer(int id, PlayerInventory playerInventory, boolean localWorld, PlayerEntity playerIn)
    {
        super(TYPE, id);
        this.isLocalWorld = localWorld;
        this.field_82862_h = playerIn;
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.field_75181_e, this.field_75179_f, 0, 154, 28));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlot(new Slot(this.field_75181_e, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for (int k = 0; k < 4; ++k)
        {
            final EquipmentSlotType equipmentslottype = PlayerContainer.VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new Slot(playerInventory, 39 - k, 8, 8 + k * 18)
            {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
                 * the case of armor slots)
                 */
                public int getSlotStackLimit()
                {
                    return 1;
                }

                /**
                 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
                 */
                public boolean isItemValid(ItemStack stack)
                {
                    return stack.canEquip(equipmentslottype, field_82862_h);
                }

                /**
                 * Return whether this slot's stack can be taken from this slot.
                 */
                public boolean canTakeStack(PlayerEntity playerIn)
                {
                    ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }

                @Nullable
                @OnlyIn(Dist.CLIENT)
                public String getSlotTexture()
                {
                    return PlayerContainer.ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()];
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

        this.addSlot(new Slot(playerInventory, 40, 77, 62)
        {
            @Nullable
            @OnlyIn(Dist.CLIENT)
            public String getSlotTexture()
            {
                return "item/empty_armor_slot_shield";
            }
        });

        ExtensionSlotBelt container = playerIn.getCapability(ExtensionSlotBelt.CAPABILITY)
                .orElseThrow(() -> new RuntimeException("Item handler not present."));

        extensionSlot = container.getBelt();

        this.addSlot(slotBelt = new ExtensionSlotSlot(extensionSlot, 77, 44)
        {
            {
                setBackgroundLocation(SLOT_BACKGROUND);
            }

            @Nullable
            @Override
            public TextureAtlasSprite getBackgroundSprite()
            {
                return new TextureAtlasSprite(SLOT_BACKGROUND, 16, 16)
                {
                    {
                        func_217789_a(16, 16, 0, 0);
                    }
                };
            }
        });

        if (!localWorld)
        {
            ToolBelt.channel.sendToServer(new ContainerSlotsHack());
        }
    }

    @Override
    public void func_201771_a(RecipeItemHelper p_201771_1_)
    {
        this.field_75181_e.fillStackedContents(p_201771_1_);
    }

    @Override
    public void clear()
    {
        this.field_75179_f.clear();
        this.field_75181_e.clear();
    }

    @Override
    public boolean matches(IRecipe<? super CraftingInventory> p_201769_1_)
    {
        return p_201769_1_.matches(this.field_75181_e, this.field_82862_h.world);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        WorkbenchContainer.func_217066_a(this.windowId, this.field_82862_h.world, this.field_82862_h, this.field_75181_e, this.field_75179_f);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        this.field_75179_f.clear();
        if (!playerIn.world.isRemote)
        {
            this.clearContainer(playerIn, playerIn.world, this.field_75181_e);
        }
        if (!playerIn.world.isRemote)
            BeltFinder.sendSync(playerIn);
    }


    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.field_75179_f && super.canMergeSlot(stack, slotIn);
    }

    @Override
    public int getOutputSlot()
    {
        return 0;
    }

    @Override
    public int getWidth()
    {
        return this.field_75181_e.getWidth();
    }

    @Override
    public int getHeight()
    {
        return this.field_75181_e.getHeight();
    }

    @Override
    public int getSize()
    {
        return 5;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack remaining = ItemStack.EMPTY;
            ItemStack slotContents = slot.getStack();
            remaining = slotContents.copy();

            if (index == slotBelt.slotNumber)
            {
                if (!this.mergeItemStack(slotContents, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }

                return remaining;
            }
            else if (slot.isItemValid(slotContents))
            {
                if (!this.mergeItemStack(slotContents, slotBelt.slotNumber, slotBelt.slotNumber + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
        }

        ItemStack itemstack = ItemStack.EMPTY;
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemstack);
            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 1 && index < 5)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 5 && index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR && !this.inventorySlots.get(8 - equipmentslottype.getIndex()).getHasStack())
            {
                int i = 8 - equipmentslottype.getIndex();
                if (!this.mergeItemStack(itemstack1, i, i + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(45).getHasStack())
            {
                if (!this.mergeItemStack(itemstack1, 45, 46, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 9 && index < 36)
            {
                if (!this.mergeItemStack(itemstack1, 36, 45, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 36 && index < 45)
            {
                if (!this.mergeItemStack(itemstack1, 9, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 9, 45, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
            if (index == 0)
            {
                playerIn.dropItem(itemstack2, false);
            }
        }

        return itemstack;
    }
}