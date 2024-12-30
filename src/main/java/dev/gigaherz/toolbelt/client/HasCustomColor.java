package dev.gigaherz.toolbelt.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HasCustomColor implements ConditionalItemModelProperty
{
    public static final HasCustomColor INSTANCE = new HasCustomColor();
    public static final MapCodec<HasCustomColor> CODEC = MapCodec.unit(INSTANCE);

    private HasCustomColor()
    {
    }

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext)
    {
        return itemStack.has(DataComponents.DYED_COLOR);
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type()
    {
        return CODEC;
    }
}
