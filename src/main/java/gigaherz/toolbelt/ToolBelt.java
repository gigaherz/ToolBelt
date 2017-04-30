package gigaherz.toolbelt;

import gigaherz.common.ItemRegistered;
import gigaherz.toolbelt.belt.ItemToolBelt;
import gigaherz.toolbelt.common.GuiHandler;
import gigaherz.toolbelt.network.SwapItems;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod.EventBusSubscriber
@Mod(modid = ToolBelt.MODID,
        version = ToolBelt.VERSION,
        acceptedMinecraftVersions = "[1.11.0,1.12.0)",
        dependencies = "after:baubles;after:jei@[4.2.6,)",
        guiFactory = "gigaherz.toolbelt.client.config.ConfigGuiFactory")
public class ToolBelt
{
    public static final String MODID = "toolbelt";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = MODID;

    public static ItemToolBelt belt;
    public static Item pouch;

    @Mod.Instance(value = ToolBelt.MODID)
    public static ToolBelt instance;

    @SidedProxy(clientSide = "gigaherz.toolbelt.client.ClientProxy", serverSide = "gigaherz.toolbelt.server.ServerProxy")
    public static ISideProxy proxy;

    public static Logger logger;

    public static GuiHandler guiHandler;

    public static SimpleNetworkWrapper channel;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                belt = new ItemToolBelt("belt"),
                pouch = new ItemRegistered("pouch").setCreativeTab(CreativeTabs.TOOLS)
        );
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SwapItems.Handler.class, SwapItems.class, messageNumber++, Side.SERVER);
        logger.debug("Final message number: " + messageNumber);

        File configurationFile = event.getSuggestedConfigurationFile();
        Config.loadConfig(configurationFile);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler = new GuiHandler());

        GameRegistry.addRecipe(new ItemStack(belt),
                "sls",
                "l l",
                "lil",
                'l', Items.LEATHER,
                'i', Items.IRON_INGOT,
                's', Items.STRING);

        GameRegistry.addRecipe(new ItemStack(pouch),
                "sgs",
                "l l",
                "sls",
                'l', Items.LEATHER,
                'g', Items.GOLD_INGOT,
                's', Items.STRING);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Config.postInit();
    }

    @SubscribeEvent
    public static void anvilChange(AnvilUpdateEvent ev)
    {
        ItemStack left = ev.getLeft();
        ItemStack right = ev.getRight();
        if (left.getCount() <= 0 || left.getItem() != belt)
            return;
        if (right.getCount() <= 0 || right.getItem() != pouch)
            return;
        int cost = ItemToolBelt.getUpgradeXP(left);
        if (cost < 0)
        {
            ev.setCanceled(true);
            return;
        }
        ev.setCost(cost);
        ev.setMaterialCost(1);

        ev.setOutput(ItemToolBelt.upgrade(left));
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
