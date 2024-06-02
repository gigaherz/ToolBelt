package dev.gigaherz.toolbelt.network;

import dev.gigaherz.toolbelt.BeltFinder;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

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

            IItemHandlerModifiable cap = (IItemHandlerModifiable)Objects.requireNonNull(stack.getCapability(Capabilities.ItemHandler.ITEM),"No inventory!");
            if (swapWith < 0)
            {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemHandlerHelper.insertItem(cap, inHand, false));
            }
            else
            {
                ItemStack inSlot = cap.getStackInSlot(swapWith);
                player.setItemInHand(InteractionHand.MAIN_HAND, inSlot);
                cap.setStackInSlot(swapWith, inHand);
            }
            getter.syncToClients();
        });
    }
}
