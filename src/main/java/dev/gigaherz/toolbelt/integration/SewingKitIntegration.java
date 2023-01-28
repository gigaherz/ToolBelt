package dev.gigaherz.toolbelt.integration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;

public class SewingKitIntegration
{
    public static void init(IEventBus modEventBus)
    {
        modEventBus.addListener(SewingKitIntegration::register);
    }

    private static void register(RegisterEvent event)
    {
        event.register(Registries.RECIPE_SERIALIZER, helper ->
                helper.register("sewing_upgrade", new SewingUpgradeRecipe.Serializer()));
    }
}
