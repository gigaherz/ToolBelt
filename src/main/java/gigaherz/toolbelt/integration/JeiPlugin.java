/*
 * Based on the 1.11 JEI Anvil recipes code.
 * License and attribution on the anvil/JEI LICENSE.txt file.
 */
package gigaherz.toolbelt.integration;

import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.integration.anvil.BeltUpgradeRecipeCategory;
import gigaherz.toolbelt.integration.anvil.BeltUpgradeRecipeHandler;
import gigaherz.toolbelt.integration.anvil.BeltUpgradeRecipeWrapper;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

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
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registry.addRecipeCategories(
                new BeltUpgradeRecipeCategory(guiHelper)
        );

        registry.addRecipeHandlers(
                new BeltUpgradeRecipeHandler()
        );

        registry.addRecipeClickArea(GuiRepair.class, 102, 48, 22, 15, BeltUpgradeRecipeCategory.UID);

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();

        recipeTransferRegistry.addRecipeTransferHandler(ContainerRepair.class, BeltUpgradeRecipeCategory.UID, 0, 2, 3, 36);

        registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.ANVIL), BeltUpgradeRecipeCategory.UID);

        ItemStack pouch = new ItemStack(ToolBelt.pouch);
        registry.addRecipes(Arrays.asList(
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(0), pouch, ToolBelt.belt.of(1), ItemToolBelt.xpCost[0]),
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(1), pouch, ToolBelt.belt.of(2), ItemToolBelt.xpCost[1]),
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(2), pouch, ToolBelt.belt.of(3), ItemToolBelt.xpCost[2]),
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(3), pouch, ToolBelt.belt.of(4), ItemToolBelt.xpCost[3]),
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(4), pouch, ToolBelt.belt.of(5), ItemToolBelt.xpCost[4]),
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(5), pouch, ToolBelt.belt.of(6), ItemToolBelt.xpCost[5]),
                new BeltUpgradeRecipeWrapper(ToolBelt.belt.of(6), pouch, ToolBelt.belt.of(7), ItemToolBelt.xpCost[6])));

    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
}
