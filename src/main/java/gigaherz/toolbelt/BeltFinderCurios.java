package gigaherz.toolbelt;

/*
import gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class BeltFinderCurios extends BeltFinder
{
    @CapabilityInject(ICurioItemHandler.class)
    public static void initBaubles(Capability<?> cap)
    {
        BeltFinder.addFinder(new BeltFinderCurios());
        ConfigData.curiosPresent = true;
    }

    @Override
    public String getName()
    {
        return "curios";
    }

    @Override
    public void setToSlot(LivingEntity player, int slot, ItemStack stack)
    {
        CuriosAPI.getCuriosHandler(player).ifPresent((curios) -> {
            CurioStackHandler handler = curios.getStackHandler("belt");
            handler.setStackInSlot(slot, stack);
        });
    }

    @Nullable
    public LazyOptional<BeltGetter> findStack(PlayerEntity player)
    {
        return CuriosAPI.getCuriosHandler(player).map((curios) -> {
            CurioStackHandler handler = curios.getStackHandler("belt");
            for (int i = 0; i < handler.getSlots(); i++)
            {
                ItemStack inSlot = handler.getStackInSlot(i);
                if (inSlot.getCount() > 0)
                {
                    if (inSlot.getItem() instanceof ToolBeltItem)
                    {
                        return Optional.of(new CuriosBeltGetter(player, i));
                    }
                }
            }

            return Optional.<BeltGetter>empty();
        }).filter(Optional::isPresent).map(Optional::get);
    }

    private class CuriosBeltGetter implements BeltGetter
    {
        private final PlayerEntity thePlayer;
        private final int slotNumber;

        private CuriosBeltGetter(PlayerEntity thePlayer, int slotNumber)
        {
            this.thePlayer = thePlayer;
            this.slotNumber = slotNumber;
        }

        @Override
        public ItemStack getBelt()
        {
            return CuriosAPI.getCuriosHandler(thePlayer).map((curios) -> {
                CurioStackHandler handler = curios.getStackHandler("belt");
                return handler.getStackInSlot(slotNumber);
            }).orElse(ItemStack.EMPTY);
        }

        @Override
        public void syncToClients()
        {
            // No need! Curios does its own sync. I think.
        }
    }
}*/