package gigaherz.toolbelt;

import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.ContainerSlotsHack;
import gigaherz.toolbelt.network.SyncBeltSlotContents;

public interface ISideProxy
{
    default void init()
    {
    }

    default void handleBeltContentsChange(BeltContentsChange message)
    {
    }

    default void handleBeltSlotContents(SyncBeltSlotContents message)
    {
    }
}
