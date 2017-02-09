package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod.EventBusSubscriber
@Mod(modid = ToolBelt.MODID,
        version = ToolBelt.VERSION,
        acceptedMinecraftVersions = "[1.9.4,1.11.0)",
        dependencies = "required-after:Forge@[12.16.0.1825,)")
public class ToolBelt
{
    public static final String MODID = "toolbelt";
    public static final String VERSION = "@VERSION@";

    public static Item belt;

    @Mod.Instance(value = ToolBelt.MODID)
    public static ToolBelt instance;

    @SidedProxy(clientSide = "gigaherz.toolbelt.client.ClientProxy", serverSide = "gigaherz.toolbelt.server.ServerProxy")
    public static ISideProxy proxy;

    public static Logger logger;

    public static GuiHandler guiHandler;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                belt = new ItemToolBelt("belt")
        );
    }

    public static void registerTileEntities()
    {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        registerTileEntities();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
        GameRegistry.addShapelessRecipe(new ItemStack(belt, 1), Items.SLIME_BALL, Items.STRING, Items.PAPER);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler = new GuiHandler());
    }
}
