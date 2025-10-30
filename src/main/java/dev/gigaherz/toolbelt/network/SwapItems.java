package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.common.ItemContainerWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SwapItems(int swapWith) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ToolBelt.location("swap_items");
    public static final Type<SwapItems> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, SwapItems> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(SwapItems::new, SwapItems::swapWith);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() -> swapItem(swapWith, context.player()));
    }

    public static void swapItem(int swapWith, Player player)
    {
        BeltFinder.findBelt(player).ifPresent((getter) -> {
            ItemStack stack = getter.getBelt();
            if (stack.getCount() <= 0)
                return;

            ItemStack inHand = player.getMainHandItem();

            if (!ConfigData.isItemStackAllowed(inHand))
                return;

            var inventory = stack.get(DataComponents.CONTAINER);
            var inventorySlots = ToolBeltItem.getBeltSize(stack);
            var wrapper = new ItemContainerWrapper(inventory, inventorySlots, stack, getter);
            if (swapWith < 0)
            {
                for(int i=0;i<wrapper.getContainerSize();i++)
                {
                    ItemStack inSlot = wrapper.getItem(i);
                    if (ItemEntity.areMergable(inSlot, inHand))
                    {
                        var max = inSlot.getMaxStackSize();
                        var acc = inSlot.getCount() + inHand.getCount();
                        if (acc <= max)
                        {
                            inHand = ItemStack.EMPTY;
                            var newStack = inSlot.copyWithCount(inSlot.getCount() + inHand.getCount());
                            wrapper.setItem(i, newStack);
                            break;
                        }
                        else
                        {
                            inHand = inHand.copyWithCount(acc - max);
                            var newStack = inSlot.copyWithCount(max);
                            wrapper.setItem(i, newStack);
                        }
                    }
                    else if (inSlot.isEmpty())
                    {
                        inHand = ItemStack.EMPTY;
                        wrapper.setItem(i, inHand);
                        break;
                    }
                }

                player.setItemInHand(InteractionHand.MAIN_HAND, inHand);
            }
            else
            {
                var inSlot = wrapper.getItem(swapWith);
                player.setItemInHand(InteractionHand.MAIN_HAND, inSlot);
                wrapper.setItem(swapWith, inHand);
            }
            getter.syncToClients();
        });
    }
}
