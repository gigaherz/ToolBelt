package dev.gigaherz.toolbelt;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

import dev.gigaherz.toolbelt.BeltFinder.BeltGetter;

public class BeltFinderCurios extends BeltFinder
{
    @CapabilityInject(ICuriosItemHandler.class)
    public static void initBaubles(Capability<?> cap)
    {
        BeltFinder.addFinder(new BeltFinderCurios());
    }

    @Override
    public String getName()
    {
        return "curios";
    }

    @Override
    public Optional<? extends BeltGetter> findStack(LivingEntity entity, boolean allowCosmetic)
    {
        return entity.getCapability(CuriosCapability.INVENTORY).resolve()
                .flatMap(curios -> curios.getCurios().entrySet().stream()
                        .map(pair -> {
                            String slotName = pair.getKey();
                            ICurioStacksHandler handler = pair.getValue();
                            if (allowCosmetic)
                            {
                                Optional<? extends BeltGetter> result = findBeltInInventory(entity, slotName, true, handler.getCosmeticStacks());
                                if (result.isPresent())
                                    return result;
                            }

                            return findBeltInInventory(entity, slotName, false, handler.getStacks());
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst());
    }

    private Optional<? extends BeltGetter> findBeltInInventory(LivingEntity entity, String slotName, boolean isCosmetic, IItemHandler inventory)
    {
        return findBeltInInventory(inventory, i -> new CuriosBeltGetter(entity, slotName, isCosmetic, i));
    }

    @Override
    protected Optional<BeltGetter> getSlotFromId(PlayerEntity entity, JsonElement slot)
    {
        return Optional.empty();
    }

    private static class CuriosBeltGetter implements BeltGetter
    {
        private final LivingEntity entity;
        private final String slotKind;
        private final boolean isCosmeticSlot;
        private final int slotNumber;

        private CuriosBeltGetter(LivingEntity entity, String slotKind, boolean isCosmeticSlot, int slotNumber)
        {
            this.entity = entity;
            this.slotKind = slotKind;
            this.isCosmeticSlot = isCosmeticSlot;
            this.slotNumber = slotNumber;
        }

        private Optional<IDynamicStackHandler> getCuriosInventory()
        {
            return getCuriosHandler()
                    .map(handler -> (isCosmeticSlot ? handler.getCosmeticStacks() : handler.getStacks()));
        }

        private Optional<ICurioStacksHandler> getCuriosHandler()
        {
            return entity.getCapability(CuriosCapability.INVENTORY).resolve()
                    .flatMap((curios) -> curios.getStacksHandler(slotKind));
        }

        @Override
        public ItemStack getBelt()
        {
            return getCuriosInventory().map(inventory -> inventory.getStackInSlot(slotNumber)).orElse(ItemStack.EMPTY);
        }

        @Override
        public void setBelt(ItemStack stack)
        {
            getCuriosInventory().ifPresent(inventory -> inventory.setStackInSlot(slotNumber, stack));
        }

        @Override
        public boolean isHidden()
        {
            return !getCuriosHandler().map(handler -> handler.isVisible() && handler.getRenders().get(slotNumber)).orElse(true);
        }

        @Override
        public void syncToClients()
        {
            // No need! Curios does its own sync. I think.
        }
    }
}