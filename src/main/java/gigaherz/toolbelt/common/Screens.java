package gigaherz.toolbelt.common;

import gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Screens
{
    private static class BeltContainerProvider implements INamedContainerProvider
    {
        private final int slot;

        private BeltContainerProvider(int slot)
        {
            this.slot = slot;
        }

        @Nullable
        @Override
        public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity player)
        {
            ItemStack heldItem = playerInventory.getStackInSlot(slot);

            int blockedSlot = -1;
            if (player.getHeldItemMainhand() == heldItem)
                blockedSlot = playerInventory.currentItem;

            return new BeltContainer(i, playerInventory, blockedSlot, heldItem);
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return new TranslationTextComponent("text.toolbelt.belt_container.title");
        }
    }

    private static class BeltSlotContainerProvider implements INamedContainerProvider
    {
        @Nullable
        @Override
        public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity player)
        {
            return new BeltSlotContainer(i, playerInventory, !player.world.isRemote);
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return new TranslationTextComponent("text.toolbelt.belt_slot_container.title");
        }
    }

    public static void openBeltScreen(ServerPlayerEntity player, int slot)
    {
        ItemStack heldItem = player.inventory.getStackInSlot(slot);
        if (heldItem.getCount() > 0 && heldItem.getItem() instanceof ToolBeltItem)
        {
            NetworkHooks.openGui(player, new BeltContainerProvider(slot), (data) -> {
                data.writeVarInt(slot);
                data.writeItemStack(heldItem);
            });
        }
    }

    public static void openSlotScreen(ServerPlayerEntity player)
    {
        player.openContainer(new BeltSlotContainerProvider());

        player.openContainer(new SimpleNamedContainerProvider(
                (i, playerInventory, playerEntity) -> new BeltSlotContainer(i, playerInventory, !playerEntity.world.isRemote),
                new TranslationTextComponent("text.toolbelt.belt_slot_container.title")
        ));
    }
}
