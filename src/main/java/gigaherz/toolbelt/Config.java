package gigaherz.toolbelt;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class Config
{
    private static Configuration config;
    public static ConfigCategory display;
    public static ConfigCategory input;
    public static ConfigCategory behaviour;

    private static final Set<String> blackListString = Sets.newHashSet();
    private static final Set<String> whiteListString = Sets.newHashSet();
    private static final Set<ItemStack> blackList = Sets.newHashSet();
    private static final Set<ItemStack> whiteList = Sets.newHashSet();

    public static boolean showBeltOnPlayers = true;
    public static float beltItemScale = 0.5f;

    public static boolean releaseToSwap = false;
    public static boolean clipMouseToCircle = true;
    public static boolean allowClickOutsideBounds = true;

    public static boolean disableAnvilUpgrading = false;

    static void loadConfig(File configurationFile)
    {
        config = new Configuration(configurationFile);

        Property bl = config.get("items", "blacklist", new String[0]);
        bl.setComment("List of items to disallow from placing in the belt.");

        Property wl = config.get("items", "whitelist", new String[0]);
        wl.setComment("List of items to force-allow placing in the belt. Takes precedence over blacklist.");

        Property showBeltOnPlayersProperty = config.get("display", "showBeltOnPlayers", true);
        showBeltOnPlayersProperty.setComment("If set to FALSE, the belts and tools will NOT draw on players.");

        Property beltItemScaleProperty = config.get("display", "beltItemScale", 0.5);
        beltItemScaleProperty.setComment("Changes the scale of items on the belt.");

        Property releaseToSwapProperty = config.get("input", "releaseToSwap", false);
        releaseToSwapProperty.setComment("If set to TRUE, releasing the menu key (R) will activate the swap. Requires a click otherwise (default).");

        Property clipMouseToCircleProperty = config.get("input", "clipMouseToCircle", false);
        clipMouseToCircleProperty.setComment("If set to TRUE, the radial menu will try to prevent the mouse from leaving the outer circle.");

        Property allowClickOutsideBoundsProperty = config.get("input", "allowClickOutsideBounds", false);
        allowClickOutsideBoundsProperty.setComment("If set to TRUE, the radial menu will allow clicking outside the outer circle to activate the items.");

        Property disableAnvilUpgradingProperty = config.get("behaviour", "disableAnvilUpgrading", false);
        disableAnvilUpgradingProperty.setComment("If set to TRUE, the internal anvil upgrade will not work, and alternative methods for upgrades will have to be provided externally.");

        display = config.getCategory("display");
        display.setComment("Options for customizing the display of tools on the player");

        input = config.getCategory("input");
        input.setComment("Options for customizing the interaction with the radial menu");

        behaviour = config.getCategory("behaviour");
        behaviour.setComment("Options for customizing the mechanics of the belt");

        showBeltOnPlayers = showBeltOnPlayersProperty.getBoolean();
        beltItemScale = (float) beltItemScaleProperty.getDouble();

        releaseToSwap = releaseToSwapProperty.getBoolean();
        clipMouseToCircle = clipMouseToCircleProperty.getBoolean();
        allowClickOutsideBounds = allowClickOutsideBoundsProperty.getBoolean();

        disableAnvilUpgrading = disableAnvilUpgradingProperty.getBoolean();

        blackListString.addAll(Arrays.asList(bl.getStringList()));
        whiteListString.addAll(Arrays.asList(wl.getStringList()));
        if (!bl.wasRead() ||
                !wl.wasRead() ||
                !releaseToSwapProperty.wasRead() ||
                !showBeltOnPlayersProperty.wasRead() ||
                !beltItemScaleProperty.wasRead() ||
                !clipMouseToCircleProperty.wasRead() ||
                !allowClickOutsideBoundsProperty.wasRead())
        {
            config.save();
        }
    }

    public static void postInit()
    {
        blackList.addAll(blackListString.stream().map(Config::parseItemStack).collect(Collectors.toList()));
        whiteList.addAll(whiteListString.stream().map(Config::parseItemStack).collect(Collectors.toList()));
    }

    public static void refresh()
    {
        showBeltOnPlayers = display.get("showBeltOnPlayers").getBoolean();
        beltItemScale = (float) display.get("beltItemScale").getDouble();
        releaseToSwap = input.get("releaseToSwap").getBoolean();
        clipMouseToCircle = input.get("clipMouseToCircle").getBoolean();
        allowClickOutsideBounds = input.get("allowClickOutsideBounds").getBoolean();
        disableAnvilUpgrading = behaviour.get("disableAnvilUpgrading").getBoolean();
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (ToolBelt.MODID.equals(event.getModID()))
        {
            if (config.hasChanged())
                config.save();
            refresh();
        }
    }

    private static final Pattern itemRegex = Pattern.compile("^(?<item>([a-zA-Z-0-9_]+:)?[a-zA-Z-0-9_]+)(?:@((?<meta>[0-9]+)|(?<any>any)))?$");

    private static ItemStack parseItemStack(String itemString)
    {
        Matcher matcher = itemRegex.matcher(itemString);

        if (!matcher.matches())
        {
            ToolBelt.logger.warn("Could not parse item " + itemString);
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(matcher.group("item")));
        if (item == null)
        {
            ToolBelt.logger.warn("Could not parse item " + itemString);
            return ItemStack.EMPTY;
        }

        String anyString = matcher.group("meta");
        String metaString = matcher.group("meta");
        int meta = Strings.isNullOrEmpty(anyString)
                ? (Strings.isNullOrEmpty(metaString) ? 0 : Integer.parseInt(metaString))
                : OreDictionary.WILDCARD_VALUE;

        return new ItemStack(item, 1, meta);
    }

    public static boolean isItemStackAllowed(final ItemStack stack)
    {
        if (stack.getCount() <= 0)
            return true;

        if (whiteList.stream().anyMatch((s) -> OreDictionary.itemMatches(s, stack, false)))
            return true;

        if (blackList.stream().anyMatch((s) -> OreDictionary.itemMatches(s, stack, false)))
            return false;

        if (stack.getItem() instanceof ItemToolBelt)
            return false;

        if (stack.getMaxStackSize() != 1)
            return false;

        return true;
    }
}
