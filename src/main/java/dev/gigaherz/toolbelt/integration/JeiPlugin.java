package dev.gigaherz.toolbelt.integration;

import com.google.common.collect.Lists;
import dev.gigaherz.toolbelt.ConfigData;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
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
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(0), pouch, Collections.singletonList(ToolBeltItem.of(1)), ToolBelt.location("upgrade_0_to_1")),
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(1), pouch, Collections.singletonList(ToolBeltItem.of(2)), ToolBelt.location("upgrade_1_to_2")),
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(2), pouch, Collections.singletonList(ToolBeltItem.of(3)), ToolBelt.location("upgrade_2_to_3")),
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(3), pouch, Collections.singletonList(ToolBeltItem.of(4)), ToolBelt.location("upgrade_3_to_4")),
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(4), pouch, Collections.singletonList(ToolBeltItem.of(5)), ToolBelt.location("upgrade_4_to_5")),
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(5), pouch, Collections.singletonList(ToolBeltItem.of(6)), ToolBelt.location("upgrade_5_to_6")),
                    vanillaFactory.createAnvilRecipe(ToolBeltItem.of(6), pouch, Collections.singletonList(ToolBeltItem.of(7)), ToolBelt.location("upgrade_6_to_7"))
            ));
        }
    }
}
