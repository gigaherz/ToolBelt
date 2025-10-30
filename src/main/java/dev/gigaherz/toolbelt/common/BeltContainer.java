package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class BeltContainer extends AbstractContainerMenu implements BeltFinder.BeltGetter
{
    private final Inventory playerInventory;
    private ItemStack blockedStack;
    private final int blockedSlot;
    public final int inventorySize;

    public BeltContainer(int id, Inventory inventory, RegistryFriendlyByteBuf extraData)
    {
        this(id, inventory, extraData.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
    }

    public BeltContainer(int id, Inventory playerInventory, int blockedSlot, ItemStack blockedStack)
    {
        super(ToolBelt.BELT_MENU.get(), id);
        this.playerInventory = playerInventory;
        this.blockedStack = blockedStack;
        this.blockedSlot = blockedSlot;
        if (blockedSlot >= 0 && !stillValid(playerInventory.player))
        {
            blockedStack = ItemStack.EMPTY;
        }

        ItemContainerContents inventory = stillValid(playerInventory.player) ? blockedStack.get(DataComponents.CONTAINER) : null;
        inventorySize = ToolBeltItem.getBeltSize(blockedStack);
        var wrapper = new ItemContainerWrapper(inventory, inventorySize, blockedStack, this);

        int xoff = ((9 - inventorySize) * 18) / 2;
        for (int k = 0; k < inventorySize; ++k)
        {
            this.addSlot(new Slot(wrapper, k, 8 + xoff + k * 18, 20)
            {

                @Override
                public boolean mayPlace(ItemStack stack)
                {
                    return ConfigData.isItemStackAllowed(stack);
                }
            });
        }

        bindPlayerInventory(playerInventory);
    }

    private void bindPlayerInventory(Container playerInventory)
    {
        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                int index = j1 + l * 9 + 9;
                this.addSlot(
                        blockedSlot == index
                                ? new LockedSlot(playerInventory, index, 8 + j1 * 18, l * 18 + 51)
                                : new Slot(playerInventory, index, 8 + j1 * 18, l * 18 + 51)
                );
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlot(
                    blockedSlot == i1
                            ? new LockedSlot(playerInventory, i1, 8 + i1 * 18, 109)
                            : new Slot(playerInventory, i1, 8 + i1 * 18, 109)
            );
        }
    }

    @Override
    public void removed(Player playerIn)
    {
        super.removed(playerIn);
        if (!playerIn.level().isClientSide())
            BeltFinder.sendSync(playerIn);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        if (inventorySize <= 0) return false;
        ItemStack held = playerIn.getInventory().getItem(blockedSlot);
        var equal = blockedSlot < 0 || held == blockedStack || ItemStack.isSameItemSameComponents(held, blockedStack);
        blockedStack = held;
        return equal;
    }

    @Override
    public void clicked(int slot, int button, ClickType clickType, Player player)
    {
        if (clickType == ClickType.SWAP && button == blockedSlot)
            return;
        super.clicked(slot, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index)
    {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem() || index == blockedSlot)
            return ItemStack.EMPTY;

        ItemStack containedStack = slot.getItem();
        ItemStack originalStack = containedStack.copy();

        int start;
        int end;
        boolean reverse = false;
        if (index < inventorySize)
        {
            start = inventorySize;
            end = this.slots.size();
            reverse = true;
        }
        else
        {
            start = 0;
            end = inventorySize;
        }

        if (!this.moveItemStackTo(containedStack, start, end, reverse))
        {
            return ItemStack.EMPTY;
        }

        if (containedStack.getCount() == 0)
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        return originalStack;
    }

    @Override
    public ItemStack getBelt()
    {
        return playerInventory.getItem(blockedSlot);
    }

    @Override
    public void setBelt(ItemStack stack)
    {
        playerInventory.setItem(blockedSlot, stack);
        blockedStack = stack;
    }

    @Override
    public void syncToClients()
    {

    }
}