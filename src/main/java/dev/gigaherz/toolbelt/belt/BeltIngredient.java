package dev.gigaherz.toolbelt.belt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BeltIngredient extends Ingredient
{
    public static final Codec<BeltIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("upgrade_level").forGetter(obj -> obj.level)
    ).apply(inst, BeltIngredient::new));

    public static BeltIngredient withLevel(int level)
    {
        return new BeltIngredient(level);
    }

    private final int level;

    protected BeltIngredient(int level)
    {
        super(Stream.of(new ItemValue(ToolBelt.BELT.get().of(level))));
        this.level = level;
    }

    @Override
    public boolean test(@Nullable ItemStack stack)
    {
        return stack != null && stack.getItem() == ToolBelt.BELT.get() && ToolBelt.BELT.get().getLevel(stack) == level;
    }
}
