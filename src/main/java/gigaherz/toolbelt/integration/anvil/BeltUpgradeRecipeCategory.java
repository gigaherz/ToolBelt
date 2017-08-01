/*
 * Based on the 1.11 JEI Anvil recipes code.
 * License and attribution on the JEI LICENSE.txt file.
 */
package gigaherz.toolbelt.integration.anvil;

import gigaherz.toolbelt.ToolBelt;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class BeltUpgradeRecipeCategory extends BlankRecipeCategory<BeltUpgradeRecipeWrapper>
{
    private static final String TITLE = "text." + ToolBelt.MODID + ".jei.belt.upgrade";
    public static final String UID = ToolBelt.MODID + ".anvil";

    private final IDrawable background;

    public BeltUpgradeRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation backgroundLocation = new ResourceLocation("textures/gui/container/anvil.png");
        background = guiHelper.createDrawable(backgroundLocation, 16, 40, 145, 37);
    }

    @Override
    public String getUid()
    {
        return UID;
    }

    @Override
    public String getTitle()
    {
        return I18n.format(TITLE);
    }

    @Override
    public IDrawable getBackground()
    {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, BeltUpgradeRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 10, 6);
        guiItemStacks.init(1, true, 59, 6);
        guiItemStacks.init(2, false, 117, 6);

        guiItemStacks.set(ingredients);
    }
}
