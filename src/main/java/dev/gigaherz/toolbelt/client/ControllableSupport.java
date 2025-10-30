package dev.gigaherz.toolbelt.client;
/*
import com.mojang.logging.LogUtils;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import dev.gigaherz.toolbelt.ToolBelt;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.BitSet;

public class ControllableSupport
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init()
    {
        try
        {
            ControllerEvents.INPUT.register(ControllableSupport::inputEvent);
        }
        catch(Throwable t)
        {
            LOGGER.error("Error registering event for Controllable input. Assuming the worst and disabling Controllable support.");
            ToolBelt.controllableEnabled = false;
        }
    }

    private static final BitSet buttons = new BitSet();

    private static boolean inputEvent(Controller controller, Value<Integer> integerValue, int originalButton, boolean state)
    {
        buttons.set(originalButton, state);
        return false;
    }

    @Nullable
    public static Boolean isButtonDown(KeyMapping keybind)
    {
        try
        {
            String customKey = keybind.getName() + ".custom";
            KeyAdapterBinding adapter = Controllable.getBindingRegistry().getKeyAdapters().get(customKey);
            if (adapter == null)
                return null;
            return buttons.get(adapter.getButton());
        }
        catch(Throwable t)
        {
            LOGGER.error("Error querying Controllable controller status for a keybind. Assuming the worst and disabling Controllable support.");
            ToolBelt.controllableEnabled = false;
            return null;
        }
    }
}
*/