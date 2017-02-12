package gigaherz.toolbelt;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import gigaherz.toolbelt.belt.ItemToolBelt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config
{
    private static final Set<ItemStack> blackList = Sets.newHashSet();
    private static final Set<ItemStack> whiteList = Sets.newHashSet();

    static void loadConfig(Configuration config)
    {
        Property bl = config.get("tileEntities", "blacklist", new String[0]);
        Property wl = config.get("tileEntities", "whitelist", new String[0]);

        blackList.addAll(Arrays.stream(bl.getStringList()).map(Config::parseItemStack).filter(Objects::nonNull).collect(Collectors.toList()));
        blackList.addAll(Arrays.stream(bl.getStringList()).map(Config::parseItemStack).filter(Objects::nonNull).collect(Collectors.toList()));
        if (!bl.wasRead() || !wl.wasRead())
            config.save();
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
