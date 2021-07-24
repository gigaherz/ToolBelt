package dev.gigaherz.toolbelt.integration;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SewingKitIntegration
{
    public static void init()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(RecipeSerializer.class, SewingKitIntegration::registerRecipes);
    }

    private static void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SewingUpgradeRecipe.Serializer().setRegistryName("sewing_upgrade")
        );
    }
}
