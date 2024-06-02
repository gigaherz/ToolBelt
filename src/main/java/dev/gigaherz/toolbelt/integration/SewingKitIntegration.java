package dev.gigaherz.toolbelt.integration;

import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SewingKitIntegration
{
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ToolBelt.MODID);
    public static DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>>
            SEWING_UGRADE_SERIALIZER = RECIPE_SERIALIZERS.register("sewing_upgrade", () -> new SewingUpgradeRecipe.Serializer());

    public static void init(IEventBus modEventBus)
    {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
