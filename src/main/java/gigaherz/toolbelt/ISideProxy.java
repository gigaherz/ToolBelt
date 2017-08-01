package gigaherz.toolbelt;

import gigaherz.toolbelt.network.BeltContentsChange;

public interface ISideProxy
{
    default void init()
    {
    }

    default void handleBeltContentsChange(BeltContentsChange message)
    {

    }
}
