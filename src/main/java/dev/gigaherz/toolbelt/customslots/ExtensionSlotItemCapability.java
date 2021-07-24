package dev.gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class ExtensionSlotItemCapability
{
    // Special slot IDs
    public static final ResourceLocation ANY_SLOT = new ResourceLocation("forge:any");
    public static final ImmutableSet<ResourceLocation> ANY_SLOT_LIST = ImmutableSet.of(ANY_SLOT);

    // The Capability
    @CapabilityInject(IExtensionSlotItem.class)
    public static Capability<IExtensionSlotItem> INSTANCE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IExtensionSlotItem.class);
    }
}
