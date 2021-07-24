package dev.gigaherz.toolbelt.belt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import net.minecraft.world.item.crafting.Ingredient.ItemValue;

public class BeltIngredient extends Ingredient
{
    public static final ResourceLocation NAME = ToolBelt.location("belt_upgrade_level");

    public static BeltIngredient withLevel(int level)
    {
        return new BeltIngredient(level);
    }

    private final int level;

    protected BeltIngredient(int level)
    {
        super(Stream.of(new ItemValue(ToolBelt.BELT.of(level))));
        this.level = level;
    }

    @Override
    public boolean test(@Nullable ItemStack stack)
    {
        return stack != null && stack.getItem() == ToolBelt.BELT && ToolBelt.BELT.getLevel(stack) == level;
    }

    @Override
    public JsonElement toJson()
    {
        JsonObject object = new JsonObject();
        object.addProperty("type", NAME.toString());
        object.addProperty("upgrade_level", level);
        return object;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static class Serializer
            implements IIngredientSerializer<BeltIngredient>
    {
        public static final IIngredientSerializer<? extends Ingredient> INSTANCE = new Serializer();

        @Override
        public BeltIngredient parse(FriendlyByteBuf buffer)
        {
            return new BeltIngredient(buffer.readVarInt());
        }

        @Override
        public void write(FriendlyByteBuf buffer, BeltIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.level);
        }

        @Override
        public BeltIngredient parse(JsonObject json)
        {
            return new BeltIngredient(
                    GsonHelper.getAsInt(json, "upgrade_level")
            );
        }
    }
}
