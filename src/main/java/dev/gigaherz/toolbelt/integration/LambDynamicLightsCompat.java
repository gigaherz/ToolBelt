package dev.gigaherz.toolbelt.integration;
/*
import dev.gigaherz.toolbelt.BeltFinder;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.compat.CompatLayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Range;

public class LambDynamicLightsCompat
{
    public static void init()
    {
        CompatLayer.LAYERS.add(new BeltSlotCompatLayer());
    }

    private static class BeltSlotCompatLayer implements CompatLayer
    {

        @Override
        public @Range(from = 0L, to = 15L) int getLivingEntityLuminanceFromItems(LivingEntity entity, boolean submergedInWater)
        {
            int luminance = 0;
            var inventory = BeltFinder.findBelt(entity);

            if (inventory.isPresent()) {
                var getter = inventory.get();
                var handler = getter.getBelt().getCapability(Capabilities.ItemHandler.ITEM);
                if (handler != null)
                {
                    for (var slot = 0; slot < handler.getSlots(); slot++) {
                        luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(handler.getStackInSlot(slot), submergedInWater));

                        if (luminance >= 15) {
                            break;
                        }
                    }
                }
            }

            return luminance;
        }
    }
}
*/