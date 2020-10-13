package dev.gigaherz.toolbelt.belt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.gigaherz.sewingkit.SewingKitMod;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        super(Stream.of(new SingleItemList(ToolBelt.BELT.of(level))));
        this.level = level;
    }

    @Override
    public boolean test(@Nullable ItemStack stack)
    {
        return stack != null && stack.getItem() == ToolBelt.BELT && ToolBelt.BELT.getLevel(stack) == level;
    }

    @Override
    public JsonElement serialize()
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
        public BeltIngredient parse(PacketBuffer buffer)
        {
            return new BeltIngredient(buffer.readVarInt());
        }

        @Override
        public void write(PacketBuffer buffer, BeltIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.level);
        }

        @Override
        public BeltIngredient parse(JsonObject json)
        {
            return new BeltIngredient(
                    JSONUtils.getInt(json, "upgrade_level")
            );
        }
    }
}
