package dev.gigaherz.toolbelt;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigData
{
    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static
    {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static
    {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static
    {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private static Set<ItemStack> blackList = Sets.newHashSet();
    private static Set<ItemStack> whiteList = Sets.newHashSet();

    public static boolean showBeltOnPlayers = true;
    public static float beltItemScale = 0.5f;

    public static boolean releaseToSwap = false;
    public static boolean clipMouseToCircle = true;
    public static boolean allowClickOutsideBounds = true;
    public static boolean displayEmptySlots = true;

    public static boolean enableSewingKitSupport = true;

    public static ThreeWayChoice anvilUpgrading = ThreeWayChoice.AUTO;
    public static boolean enableAnvilUpgrading = true;

    public static ThreeWayChoice gridCrafting = ThreeWayChoice.AUTO;
    public static boolean enableNormalCrafting = true;

    public static ThreeWayChoice customBeltSlotMode = ThreeWayChoice.AUTO;
    public static boolean customBeltSlotEnabled = true;

    public static boolean curiosPresent() { return ModList.get().isLoaded("curios"); }
    public static boolean sewingKitPresent() { return ModList.get().isLoaded("sewingkit"); }

    public static class ServerConfig
    {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> whitelist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklist;

        ServerConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("general");
            whitelist = builder
                    .comment("List of items to force-allow placing in the belt. Takes precedence over blacklist.")
                    .translation("text.toolbelt.config.whitelist")
                    .defineList("whitelist", Lists.newArrayList(), o -> o instanceof String);
            blacklist = builder
                    .comment("List of items to disallow from placing in the belt. (whitelist takes precedence)")
                    .translation("text.toolbelt.config.blacklist")
                    .defineList("blacklist", Lists.newArrayList(), o -> o instanceof String);
            builder.pop();
        }
    }

    // Any config that has to deal with datapack stuffs
    public static class CommonConfig
    {
        public final ForgeConfigSpec.EnumValue<ThreeWayChoice> customBeltSlotMode;
        public final ForgeConfigSpec.EnumValue<ThreeWayChoice> anvilUpgrading;
        public final ForgeConfigSpec.EnumValue<ThreeWayChoice> gridCrafting;
        public final ForgeConfigSpec.BooleanValue enableSewingKitSupport;

        CommonConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("general");
            enableSewingKitSupport = builder
                    .comment("If set to FALSE, support for sewing recipes will not be enabled regardless of the mod's presence.")
                    .translation("text.toolbelt.config.disable_anvil_update")
                    .define("disableAnvilUpgrading", false);
            anvilUpgrading = builder
                    .comment("If AUTO, the crafting and upgrade recipes will use the Sewing mechanics if the Sewing Kit mod is present. Off disables anvil upgrading regardless.")
                    .translation("text.toolbelt.config.anvil_upgrading")
                    .defineEnum("anvilUpgrading", ThreeWayChoice.AUTO);
            gridCrafting = builder
                    .comment("If AUTO, the belt and pouch crafting recipes will be disabled if the Sewing Kit mod is present, sewing recipes will be used instead.")
                    .translation("text.toolbelt.config.enable_grid_crafting")
                    .defineEnum("enableGridCraftingRecipes", ThreeWayChoice.AUTO);
            customBeltSlotMode = builder
                    .comment("If AUTO, the belt slot will be disabled if Curios is present. If OFF, the belt slot will be disabled permanently.")
                    .translation("text.toolbelt.config.custom_belt_slot_mode")
                    .defineEnum("customBeltSlotMode", ThreeWayChoice.ON);
            builder.pop();
        }
    }

    public static class ClientConfig
    {
        public final ForgeConfigSpec.BooleanValue showBeltOnPlayers;
        public final ForgeConfigSpec.DoubleValue beltItemScale;
        public final ForgeConfigSpec.BooleanValue releaseToSwap;
        public final ForgeConfigSpec.BooleanValue clipMouseToCircle;
        public final ForgeConfigSpec.BooleanValue allowClickOutsideBounds;
        public final ForgeConfigSpec.BooleanValue displayEmptySlots;

        ClientConfig(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Options for customizing the display of tools on the player")
                    .push("display");
            showBeltOnPlayers = builder
                    .comment("If set to FALSE, the belts and tools will NOT draw on players.")
                    .translation("text.toolbelt.config.show_belt_on_players")
                    .define("showBeltOnPlayers", true);
            beltItemScale = builder
                    .comment("Changes the scale of items on the belt.")
                    .translation("text.toolbelt.config.belt_item_scale")
                    .defineInRange("beltItemScale", 0.5, 0.1, 2.0);
            builder.pop();
            builder.comment("Options for customizing the radial menu")
                    .push("menu");
            releaseToSwap = builder
                    .comment("If set to TRUE, releasing the menu key (R) will activate the swap. Requires a click otherwise (default).")
                    .translation("text.toolbelt.config.release_to_swap")
                    .define("releaseToSwap", false);
            clipMouseToCircle = builder
                    .comment("If set to TRUE, the radial menu will try to prevent the mouse from leaving the outer circle.")
                    .translation("text.toolbelt.config.clip_mouse_to_circle")
                    .define("clipMouseToCircle", false);
            allowClickOutsideBounds = builder
                    .comment("If set to TRUE, the radial menu will allow clicking outside the outer circle to activate the items.")
                    .translation("text.toolbelt.config.click_outside_bounds")
                    .define("allowClickOutsideBounds", false);
            displayEmptySlots = builder
                    .comment("If set to TRUE, the radial menu will always display all the slots, even when empty, and will allow choosing which empty slot to insert into.")
                    .translation("text.toolbelt.config.display_empty_slots")
                    .define("displayEmptySlots", false);
            builder.pop();
        }
    }

    public static void refreshClient()
    {
        showBeltOnPlayers = CLIENT.showBeltOnPlayers.get();
        beltItemScale = (float) (double) CLIENT.beltItemScale.get();
        releaseToSwap = CLIENT.releaseToSwap.get();
        clipMouseToCircle = CLIENT.clipMouseToCircle.get();
        allowClickOutsideBounds = CLIENT.allowClickOutsideBounds.get();
        displayEmptySlots = CLIENT.displayEmptySlots.get();
    }

    public static void refreshServer()
    {
        enableSewingKitSupport = COMMON.enableSewingKitSupport.get();

        anvilUpgrading = COMMON.anvilUpgrading.get();
        enableAnvilUpgrading = anvilUpgrading == ThreeWayChoice.ON ||
                (anvilUpgrading == ThreeWayChoice.AUTO && !sewingKitPresent());

        gridCrafting = COMMON.gridCrafting.get();
        enableNormalCrafting = gridCrafting == ThreeWayChoice.ON ||
                (gridCrafting == ThreeWayChoice.AUTO && !sewingKitPresent());

        customBeltSlotMode = COMMON.customBeltSlotMode.get();
        customBeltSlotEnabled = customBeltSlotMode == ThreeWayChoice.ON ||
                (customBeltSlotMode == ThreeWayChoice.AUTO && !curiosPresent());

        blackList = SERVER.blacklist.get().stream().map(ConfigData::parseItemStack).collect(Collectors.toSet());
        whiteList = SERVER.whitelist.get().stream().map(ConfigData::parseItemStack).collect(Collectors.toSet());
    }

    private static ItemStack parseItemStack(String itemString)
    {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemString));
        if (item == null || item == Items.AIR)
        {
            ToolBelt.logger.warn("Could not find item " + itemString);
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }

    public static boolean isItemStackAllowed(final ItemStack stack)
    {
        if (stack.getCount() <= 0)
            return true;

        if (whiteList.stream().anyMatch((s) -> ItemStack.isSame(s, stack)))
            return true;

        if (blackList.stream().anyMatch((s) -> ItemStack.isSame(s, stack)))
            return false;

        if (stack.getItem() instanceof ToolBeltItem)
            return false;

        if (stack.getMaxStackSize() != 1)
            return false;

        return true;
    }

    public enum ThreeWayChoice implements IStringSerializable
    {
        OFF("off"),
        AUTO("auto"),
        ON("on");

        private final String name;

        ThreeWayChoice(String name)
        {
            this.name = name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }

        public static ThreeWayChoice byName(String name)
        {
            for (ThreeWayChoice mode : values())
            {
                if (mode.name.equalsIgnoreCase(name))
                    return mode;
            }
            return ThreeWayChoice.ON;
        }

        public static String[] names()
        {
            return Arrays.stream(values()).map(ThreeWayChoice::getSerializedName).toArray(String[]::new);
        }
    }
}
