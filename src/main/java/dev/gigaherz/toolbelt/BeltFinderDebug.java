package dev.gigaherz.toolbelt;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Optional;

@EventBusSubscriber(modid = ToolBelt.MODID)
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
        if (!allowCosmetic || entity instanceof Player || entity instanceof ArmorStand)
            return Optional.empty();

        return Optional.of(new DebugBeltGetter());
    }

    private static class DebugBeltGetter implements BeltGetter
    {

        private DebugBeltGetter()
        {
        }

        @Override
        public ItemStack getBelt()
        {
            return new ItemStack(ToolBelt.BELT.get());
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