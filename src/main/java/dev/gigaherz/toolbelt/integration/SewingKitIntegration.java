package dev.gigaherz.toolbelt.integration;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SewingKitIntegration
{
    public static void init()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(IRecipeSerializer.class, SewingKitIntegration::registerRecipes);
    }

    private static void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SewingUpgradeRecipe.Serializer().setRegistryName("sewing_upgrade")
        );
    }
}
