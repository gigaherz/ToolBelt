package dev.gigaherz.toolbelt;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

public class Conditions
{
    public static class EnableNormalCrafting implements ICondition
    {
        public static final MapCodec<EnableNormalCrafting> CODEC = MapCodec.unit(EnableNormalCrafting::new);

        @Override
        public boolean test(IContext context)
        {
            return ConfigData.enableNormalCrafting;
        }

        @Override
        public MapCodec<? extends ICondition> codec()
        {
            return CODEC;
        }
    }

    public static class EnableSewingCrafting implements ICondition
    {
        public static final MapCodec<EnableSewingCrafting> CODEC = MapCodec.unit(EnableSewingCrafting::new);

        @Override
        public boolean test(IContext context)
        {
            return ConfigData.enableSewingKitSupport;
        }

        @Override
        public MapCodec<? extends ICondition> codec()
        {
            return CODEC;
        }
    }
}
