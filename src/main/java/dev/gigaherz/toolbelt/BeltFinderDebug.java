package dev.gigaherz.toolbelt;

import com.google.gson.JsonElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Optional;

import dev.gigaherz.toolbelt.BeltFinder.BeltGetter;

@Mod.EventBusSubscriber(modid=ToolBelt.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class BeltFinderDebug extends BeltFinder
{
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent setup)
    {
        if (!FMLEnvironment.production)
        {
            BeltFinder.addFinder(new BeltFinderDebug());
        }
    }

    @Override
    public String getName()
    {
        return "debug";
    }

    @Override
    public Optional<? extends BeltGetter> findStack(LivingEntity entity, boolean allowCosmetic)
    {
        if (!allowCosmetic || entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)
            return Optional.empty();

        return Optional.of(new DebugBeltGetter());
    }

    @Override
    protected Optional<BeltGetter> getSlotFromId(PlayerEntity player, JsonElement slot)
    {
        return Optional.empty();
    }

    private static class DebugBeltGetter implements BeltGetter
    {

        private DebugBeltGetter()
        {
        }

        @Override
        public ItemStack getBelt()
        {
            return new ItemStack(ToolBelt.BELT);
        }

        @Override
        public boolean isHidden()
        {
            return false;
        }

        @Override
        public void syncToClients()
        {
            // No need!
        }
    }
}