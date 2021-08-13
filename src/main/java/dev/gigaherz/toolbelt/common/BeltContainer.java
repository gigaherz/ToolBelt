package dev.gigaherz.toolbelt.common;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ObjectHolder;

public class BeltContainer extends AbstractContainerMenu
{
    @ObjectHolder("toolbelt:belt_container")
    public static MenuType<BeltContainer> TYPE;

    public final int beltSlots;
    private final ItemStack heldItem;
    private final int blockedSlot;

    public BeltContainer(int id, Inventory inventory, FriendlyByteBuf extraData)
    {
        this(id, inventory, extraData.readVarInt(), extraData.readItem());
    }

    public BeltContainer(int id, Inventory playerInventory, int blockedSlot, ItemStack heldItem)
    {
        super(TYPE, id);
        this.heldItem = heldItem;
        this.blockedSlot = blockedSlot;
        ToolBeltInventory beltInventory = stillValid(playerInventory.player) && heldItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .orElseThrow(() -> new RuntimeException("Item handler not present.")) instanceof ToolBeltInventory inv
                ? inv : new ToolBeltInventory(new ItemStack(ToolBelt.BELT));

        beltSlots = beltInventory.getSlots();
        int xoff = ((9 - beltSlots) * 18) / 2;
        for (int k = 0; k < beltSlots; ++k)
        {
            this.addSlot(new BeltSlot(playerInventory, heldItem, blockedSlot, k, 8 + xoff + k * 18, 20));
        }

        bindPlayerInventory(playerInventory, blockedSlot);
    }

    private void bindPlayerInventory(Container playerInventory, int blockedSlot)
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
        if (!playerIn.level.isClientSide)
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
        return blockedSlot < 0 || playerIn.getInventory().getItem(blockedSlot).equals(heldItem, false);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index)
    {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack containedStack = slot.getItem();
        ItemStack originalStack = containedStack.copy();

        int start;
        int end;
        boolean reverse = false;
        if (index < beltSlots)
        {
            start = beltSlots;
            end = this.slots.size();
            reverse = true;
        }
        else
        {
            start = 0;
            end = beltSlots;
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
}