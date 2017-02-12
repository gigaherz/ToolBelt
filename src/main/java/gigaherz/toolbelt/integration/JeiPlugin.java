package gigaherz.toolbelt.integration;

import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;

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
    public void register(IModRegistry registry)
    {
        /*registry.addRecipes(
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(0), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(1), ItemToolBelt.xpCost[0]),
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(1), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(2), ItemToolBelt.xpCost[1]),
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(2), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(3), ItemToolBelt.xpCost[2]),
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(3), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(4), ItemToolBelt.xpCost[3]),
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(4), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(5), ItemToolBelt.xpCost[4]),
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(5), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(6), ItemToolBelt.xpCost[5]),
                new AnvilRecipeWrapper(ToolBelt.belt.getStack(6), new ItemStack(ToolBelt.pouch), ToolBelt.belt.getStack(7), ItemToolBelt.xpCost[6]));*/
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
}
