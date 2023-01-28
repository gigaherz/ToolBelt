package dev.gigaherz.toolbelt.integration;
/*
import com.google.common.collect.Lists;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    private static final ResourceLocation ID = ToolBelt.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        if (ConfigData.enableAnvilUpgrading)
        {
            IVanillaRecipeFactory vanillaFactory = registration.getVanillaRecipeFactory();
            List<ItemStack> pouch = Collections.singletonList(new ItemStack(ToolBelt.POUCH.get()));
            registration.addRecipes(RecipeTypes.ANVIL, Lists.newArrayList(
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(0), pouch, Collections.singletonList(ToolBelt.BELT.get().of(1))),
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(1), pouch, Collections.singletonList(ToolBelt.BELT.get().of(2))),
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(2), pouch, Collections.singletonList(ToolBelt.BELT.get().of(3))),
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(3), pouch, Collections.singletonList(ToolBelt.BELT.get().of(4))),
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(4), pouch, Collections.singletonList(ToolBelt.BELT.get().of(5))),
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(5), pouch, Collections.singletonList(ToolBelt.BELT.get().of(6))),
                    vanillaFactory.createAnvilRecipe(ToolBelt.BELT.get().of(6), pouch, Collections.singletonList(ToolBelt.BELT.get().of(7)))
            ));
        }
    }
}
*/