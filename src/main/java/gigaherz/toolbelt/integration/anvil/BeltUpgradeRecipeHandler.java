/*
 * Based on the 1.11 JEI Anvil recipes code.
 * License and attribution on the JEI LICENSE.txt file.
 */
package gigaherz.toolbelt.integration.anvil;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class BeltUpgradeRecipeHandler implements IRecipeHandler<BeltUpgradeRecipeWrapper>
{

    @Override
    public Class<BeltUpgradeRecipeWrapper> getRecipeClass()
    {
        return BeltUpgradeRecipeWrapper.class;
    }

    @Deprecated
    @Override
    public String getRecipeCategoryUid()
    {
        return BeltUpgradeRecipeCategory.UID;
    }

    @Override
    public String getRecipeCategoryUid(BeltUpgradeRecipeWrapper recipe)
    {
        return BeltUpgradeRecipeCategory.UID;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(BeltUpgradeRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(BeltUpgradeRecipeWrapper recipe)
    {
        return true;
    }
}
