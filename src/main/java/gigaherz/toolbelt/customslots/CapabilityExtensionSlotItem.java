package gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityExtensionSlotItem
{
    // Special slot IDs
    public static final ResourceLocation ANY_SLOT = new ResourceLocation("forge:any");
    public static final ImmutableSet<ResourceLocation> ANY_SLOT_LIST = ImmutableSet.of(ANY_SLOT);

    // The Capability
    @CapabilityInject(IExtensionSlotItem.class)
    public static Capability<IExtensionSlotItem> INSTANCE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IExtensionSlotItem.class, new Storage(), DefaultImplementation::new);
    }

    static class Storage implements Capability.IStorage<IExtensionSlotItem>
    {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IExtensionSlotItem> capability, IExtensionSlotItem instance, Direction side)
        {
            return null;
        }

        @Override
        public void readNBT(Capability<IExtensionSlotItem> capability, IExtensionSlotItem instance, Direction side, INBT nbt)
        {

        }
    }

    private static class DefaultImplementation implements IExtensionSlotItem
    {
    }
}

