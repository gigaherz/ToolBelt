package gigaherz.toolbelt.integration;

import gigaherz.toolbelt.ToolBelt;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
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
    public void register(IModRegistry registry)
    {
        List<ItemStack> pouch = Collections.singletonList(new ItemStack(ToolBelt.pouch));
        registry.addAnvilRecipe(ToolBelt.belt.of(0), pouch, Collections.singletonList(ToolBelt.belt.of(1)));
        registry.addAnvilRecipe(ToolBelt.belt.of(1), pouch, Collections.singletonList(ToolBelt.belt.of(2)));
        registry.addAnvilRecipe(ToolBelt.belt.of(2), pouch, Collections.singletonList(ToolBelt.belt.of(3)));
        registry.addAnvilRecipe(ToolBelt.belt.of(3), pouch, Collections.singletonList(ToolBelt.belt.of(4)));
        registry.addAnvilRecipe(ToolBelt.belt.of(4), pouch, Collections.singletonList(ToolBelt.belt.of(5)));
        registry.addAnvilRecipe(ToolBelt.belt.of(5), pouch, Collections.singletonList(ToolBelt.belt.of(6)));
        registry.addAnvilRecipe(ToolBelt.belt.of(6), pouch, Collections.singletonList(ToolBelt.belt.of(7)));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
}
