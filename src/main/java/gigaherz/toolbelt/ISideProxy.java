package gigaherz.toolbelt;

import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.ContainerSlotsHack;

public interface ISideProxy
{
    default void init()
    {
    }

    default void handleBeltContentsChange(BeltContentsChange message)
    {
    }

    default void handleContainerSlots(ContainerSlotsHack message)
    {
    }
}
