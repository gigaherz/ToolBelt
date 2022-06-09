package dev.gigaherz.toolbelt.integration;

import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

public class SewingKitIntegration
{
    public static void init()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(SewingKitIntegration::registerRecipes);
    }

    private static void registerRecipes(RegisterEvent event)
    {
        event.register(Registry.RECIPE_SERIALIZER_REGISTRY, helper ->
                helper.register("sewing_upgrade", new SewingUpgradeRecipe.Serializer()));
    }
}
