package gigaherz.toolbelt.customslots;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nonnull;

public interface IExtensionContainer
{
    @Nonnull
    EntityLivingBase getOwner();

    @Nonnull
    ImmutableList<IExtensionSlot> getSlots();
}
