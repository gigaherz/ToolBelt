package dev.gigaherz.toolbelt.client;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import java.util.Map;

public class DynamicBeltModel extends BakedModelWrapper<BakedModel> {
    private final Map<Integer, BakedModel> tierModels;
    private final Map<Integer, BakedModel> tierDyedModels;
    @Nullable private final BakedModel dyedBaseModel;
    private final ItemOverrides overrides;

    public DynamicBeltModel(BakedModel base, Map<Integer, BakedModel> tierModels,
                            Map<Integer, BakedModel> tierDyedModels, @Nullable BakedModel dyedBaseModel) {
        super(base);
        this.tierModels = tierModels;
        this.tierDyedModels = tierDyedModels;
        this.dyedBaseModel = dyedBaseModel;
        this.overrides = new ItemOverrides() {
            @Override
            public @Nullable BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                return resolveModel(stack);
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return resolveModel(itemStack).getRenderPasses(itemStack, fabulous);
    }

    private BakedModel resolveModel(ItemStack stack) {
        int slots = ToolBeltItem.getSlotsCount(stack);
        boolean dyed = stack.has(DataComponents.DYED_COLOR);

        if (dyed) {
            BakedModel dyedTier = tierDyedModels.get(slots);
            if (dyedTier != null) return dyedTier;
            if (dyedBaseModel != null) return dyedBaseModel;
        }

        BakedModel tier = tierModels.get(slots);
        if (tier != null) return tier;

        return originalModel;
    }
}
