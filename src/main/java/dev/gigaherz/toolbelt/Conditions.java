package dev.gigaherz.toolbelt;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class Conditions
{
    public static class EnableNormalCrafting implements ICondition
    {
        public static final Codec<EnableNormalCrafting> CODEC = Codec.unit(EnableNormalCrafting::new);

        @Override
        public boolean test(IContext context)
        {
            return ConfigData.enableNormalCrafting;
        }

        @Override
        public Codec<? extends ICondition> codec()
        {
            return CODEC;
        }
    }

    public static class EnableSewingCrafting implements ICondition
    {
        public static final Codec<EnableSewingCrafting> CODEC = Codec.unit(EnableSewingCrafting::new);

        @Override
        public boolean test(IContext context)
        {
            return ConfigData.enableSewingKitSupport;
        }

        @Override
        public Codec<? extends ICondition> codec()
        {
            return CODEC;
        }
    }
}
