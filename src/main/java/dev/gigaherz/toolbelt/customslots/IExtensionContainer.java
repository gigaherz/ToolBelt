package dev.gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;

public interface IExtensionContainer
{
    @Nonnull
    LivingEntity getOwner();

    @Nonnull
    ImmutableList<IExtensionSlot> getSlots();

    void onContentsChanged(IExtensionSlot slot);
}
