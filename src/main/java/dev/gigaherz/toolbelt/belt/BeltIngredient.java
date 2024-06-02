package dev.gigaherz.toolbelt.belt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

public class BeltIngredient implements ICustomIngredient
{
    public static final MapCodec<BeltIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.fieldOf("size").forGetter(obj -> obj.size)
    ).apply(inst, BeltIngredient::new));

    public static BeltIngredient withLevel(int level)
    {
        return new BeltIngredient(level);
    }

    private final int size;

    protected BeltIngredient(int size)
    {
        this.size = size;
    }

    @Override
    public boolean test(ItemStack stack)
    {
        return stack.getItem() == ToolBelt.BELT.get() && ToolBeltItem.getSlotsCount(stack) == size;
    }

    @Override
    public Stream<ItemStack> getItems()
    {
        return Stream.of(ToolBelt.BELT.get().forSize(size));
    }

    @Override
    public boolean isSimple()
    {
        return false;
    }

    @Override
    public IngredientType<?> getType()
    {
        return ToolBelt.BELT_INGREDIENT.get();
    }
}
