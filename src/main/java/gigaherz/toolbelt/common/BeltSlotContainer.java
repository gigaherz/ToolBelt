package gigaherz.toolbelt.common;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.customslots.ExtensionSlotSlot;
import gigaherz.toolbelt.customslots.IExtensionSlot;
import gigaherz.toolbelt.network.ContainerSlotsHack;
import gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.RecipeBookCategories;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;

public class BeltSlotContainer extends RecipeBookContainer<CraftingInventory>
{
    @ObjectHolder("toolbelt:belt_slot_container")
    public static ContainerType<BeltSlotContainer> TYPE;

    public static final ResourceLocation SLOT_BACKGROUND = ToolBelt.location("textures/gui/empty_belt_slot_background.png");

    private final ExtensionSlotSlot slotBelt;
    private final IExtensionSlot extensionSlot;

    private final CraftingInventory craftingInventory = new CraftingInventory(this, 2, 2);
    private final CraftResultInventory craftResultInventory = new CraftResultInventory();
    public final boolean isLocalWorld;
    private final PlayerEntity player;

    private interface SlotFactory<T extends Slot>
    {
        T create(IExtensionSlot slot, int x, int y);
    }

    private SlotFactory<ExtensionSlotSlot> slotFactory = DistExecutor.runForDist(
            () -> () -> ExtensionSlotSlotClient::new,
            () -> () -> ExtensionSlotSlot::new
    );

    public BeltSlotContainer(int id, PlayerInventory playerInventory)
    {
        this(id, playerInventory, true);
    }

    public BeltSlotContainer(int id, PlayerInventory playerInventory, boolean localWorld)
    {
        super(TYPE, id);
        this.isLocalWorld = localWorld;
        this.player = playerInventory.player;
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.craftingInventory, this.craftResultInventory, 0, 154, 28));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlot(new Slot(this.craftingInventory, j + i * 2, 98 + j * 18, 18 + i * 18));
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
                    return stack.canEquip(equipmentslottype, player);
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

        BeltExtensionSlot container = playerInventory.player.getCapability(BeltExtensionSlot.CAPABILITY)
                .orElseThrow(() -> new RuntimeException("Item handler not present."));

        extensionSlot = container.getBelt();

        this.addSlot(slotBelt = slotFactory.create(BeltSlotContainer.this.extensionSlot, 77, 44));

        if (!localWorld)
        {
            ToolBelt.channel.sendToServer(new ContainerSlotsHack());
        }
    }

    @Override
    public List<RecipeBookCategories> getRecipeBookCategories()
    {
        return Lists.newArrayList(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE);
    }

    @Override
    public void func_201771_a(RecipeItemHelper p_201771_1_)
    {
        this.craftingInventory.fillStackedContents(p_201771_1_);
    }

    @Override
    public void clear()
    {
        this.craftResultInventory.clear();
        this.craftingInventory.clear();
    }

    @Override
    public boolean matches(IRecipe<? super CraftingInventory> p_201769_1_)
    {
        return p_201769_1_.matches(this.craftingInventory, this.player.world);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        WorkbenchContainer.func_217066_a(this.windowId, this.player.world, this.player, this.craftingInventory, this.craftResultInventory);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        this.craftResultInventory.clear();
        if (!playerIn.world.isRemote)
        {
            this.clearContainer(playerIn, playerIn.world, this.craftingInventory);
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
        return slotIn.inventory != this.craftResultInventory && super.canMergeSlot(stack, slotIn);
    }

    @Override
    public int getOutputSlot()
    {
        return 0;
    }

    @Override
    public int getWidth()
    {
        return this.craftingInventory.getWidth();
    }

    @Override
    public int getHeight()
    {
        return this.craftingInventory.getHeight();
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

    private class ExtensionSlotSlotClient extends ExtensionSlotSlot
    {
        {
            setBackgroundLocation(SLOT_BACKGROUND);
        }

        public ExtensionSlotSlotClient(IExtensionSlot slot, int x, int y)
        {
            super(slot, x, y);
        }

        @OnlyIn(Dist.CLIENT)
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
    }
}