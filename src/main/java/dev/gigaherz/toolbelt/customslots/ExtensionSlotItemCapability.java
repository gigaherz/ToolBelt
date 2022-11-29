package dev.gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ExtensionSlotItemCapability
{
    // Special slot IDs
    public static final ResourceLocation ANY_SLOT = new ResourceLocation("forge:any");
    public static final ImmutableSet<ResourceLocation> ANY_SLOT_LIST = ImmutableSet.of(ANY_SLOT);

    // The Capability
    public static Capability<IExtensionSlotItem> INSTANCE = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IExtensionSlotItem.class);
    }
}
