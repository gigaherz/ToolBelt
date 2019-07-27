package gigaherz.toolbelt;

import gigaherz.toolbelt.belt.ToolBeltItem;
import gigaherz.toolbelt.client.ClientEvents;
import gigaherz.toolbelt.common.BeltContainer;
import gigaherz.toolbelt.common.BeltScreen;
import gigaherz.toolbelt.common.BeltSlotContainer;
import gigaherz.toolbelt.common.BeltSlotScreen;
import gigaherz.toolbelt.customslots.ExtensionSlotItemCapability;
import gigaherz.toolbelt.network.*;
import gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ToolBelt.MODID)
public class ToolBelt
{
    public static final String MODID = "toolbelt";

    @ObjectHolder("toolbelt:belt")
    public static Item belt;

    @ObjectHolder("toolbelt:pouch")
    public static Item pouch;

    public static ToolBelt instance;

    public static final Logger logger = LogManager.getLogger(MODID);

    public static final String CHANNEL = MODID;
    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, CHANNEL))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public ToolBelt()
    {
        instance = this;

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainers);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::modConfig);

        MinecraftForge.EVENT_BUS.addListener(this::anvilChange);

        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
    }

    public void modConfig(ModConfig.ModConfigEvent event)
    {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ConfigData.CLIENT_SPEC)
            ConfigData.refreshClient();
        else if (config.getSpec() == ConfigData.SERVER_SPEC)
            ConfigData.refreshServer();
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new ToolBeltItem(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS)).setRegistryName("belt"),
                new Item(new Item.Properties().group(ItemGroup.TOOLS)).setRegistryName("pouch")
        );
    }

    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                IForgeContainerType.create(BeltSlotContainer::new).setRegistryName("belt_slot_container"),
                IForgeContainerType.create(BeltContainer::new).setRegistryName("belt_container")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        channel.registerMessage(messageNumber++, SwapItems.class, SwapItems::encode, SwapItems::new, SwapItems::handle);
        channel.registerMessage(messageNumber++, BeltContentsChange.class, BeltContentsChange::encode, BeltContentsChange::new, BeltContentsChange::handle);
        channel.registerMessage(messageNumber++, OpenBeltSlotInventory.class, OpenBeltSlotInventory::encode, OpenBeltSlotInventory::new, OpenBeltSlotInventory::handle);
        channel.registerMessage(messageNumber++, ContainerSlotsHack.class, ContainerSlotsHack::encode, ContainerSlotsHack::new, ContainerSlotsHack::handle);
        channel.registerMessage(messageNumber++, SyncBeltSlotContents.class, SyncBeltSlotContents::encode, SyncBeltSlotContents::new, SyncBeltSlotContents::handle);
        logger.debug("Final message number: " + messageNumber);

        //TODO File configurationFile = event.getSuggestedConfigurationFile();
        //Config.loadConfig(configurationFile);

        ExtensionSlotItemCapability.register();
        BeltExtensionSlot.register();
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(BeltContainer.TYPE, BeltScreen::new);
        ScreenManager.registerFactory(BeltSlotContainer.TYPE, BeltSlotScreen::new);
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> ClientEvents::initKeybinds);
    }

    public void anvilChange(AnvilUpdateEvent ev)
    {
        if (ConfigData.disableAnvilUpgrading)
            return;

        ItemStack left = ev.getLeft();
        ItemStack right = ev.getRight();
        if (left.getCount() <= 0 || left.getItem() != belt)
            return;
        if (right.getCount() <= 0 || right.getItem() != pouch)
            return;
        int cost = ToolBeltItem.getUpgradeXP(left);
        if (cost < 0)
        {
            ev.setCanceled(true);
            return;
        }
        ev.setCost(cost);
        ev.setMaterialCost(1);

        ev.setOutput(ToolBeltItem.upgrade(left));
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
