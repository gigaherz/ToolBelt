package gigaherz.toolbelt.integration;

import com.google.common.collect.Lists;
import gigaherz.toolbelt.Config;
import gigaherz.toolbelt.ToolBelt;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

@mezz.jei.api.JEIPlugin
public class JeiPlugin implements IModPlugin
{
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
    {
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry)
    {
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {

    }

    @Override
    public void register(IModRegistry registry)
    {
        if (Config.disableAnvilUpgrading)
            return;
        IJeiHelpers helpers = registry.getJeiHelpers();
        IVanillaRecipeFactory vanillaFactory = helpers.getVanillaRecipeFactory();
        List<ItemStack> pouch = Collections.singletonList(new ItemStack(ToolBelt.pouch));
        registry.addRecipes(Lists.newArrayList(
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(0), pouch, Collections.singletonList(ToolBelt.belt.of(1))),
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(1), pouch, Collections.singletonList(ToolBelt.belt.of(2))),
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(2), pouch, Collections.singletonList(ToolBelt.belt.of(3))),
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(3), pouch, Collections.singletonList(ToolBelt.belt.of(4))),
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(4), pouch, Collections.singletonList(ToolBelt.belt.of(5))),
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(5), pouch, Collections.singletonList(ToolBelt.belt.of(6))),
                vanillaFactory.createAnvilRecipe(ToolBelt.belt.of(6), pouch, Collections.singletonList(ToolBelt.belt.of(7)))
        ), VanillaRecipeCategoryUid.ANVIL);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
}
