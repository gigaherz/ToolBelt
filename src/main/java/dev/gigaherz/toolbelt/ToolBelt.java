package dev.gigaherz.toolbelt;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import dev.gigaherz.sewingkit.SewingKitMod;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.sewingkit.api.ToolActionIngredient;
import dev.gigaherz.sewingkit.needle.NeedleItem;
import dev.gigaherz.sewingkit.needle.Needles;
import dev.gigaherz.toolbelt.belt.BeltIngredient;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.common.BeltContainer;
import dev.gigaherz.toolbelt.common.BeltScreen;
import dev.gigaherz.toolbelt.slot.BeltSlotMenu;
import dev.gigaherz.toolbelt.slot.BeltSlotScreen;
import dev.gigaherz.toolbelt.integration.SewingKitIntegration;
import dev.gigaherz.toolbelt.integration.SewingUpgradeRecipeBuilder;
import dev.gigaherz.toolbelt.network.*;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(ToolBelt.MODID)
public class ToolBelt
{
    public static final String MODID = "toolbelt";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);

    private static final DeferredRegister<Codec<? extends ICondition>> CONDITION_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, ToolBelt.MODID);

    public static final DeferredHolder<Codec<? extends ICondition>, Codec<Conditions.EnableNormalCrafting>> ENABLE_NORMAL
            = CONDITION_SERIALIZERS.register("enable_normal_crafting", () -> Conditions.EnableNormalCrafting.CODEC);

    public static final DeferredHolder<Codec<? extends ICondition>, Codec<Conditions.EnableSewingCrafting>> ENABLE_SEWING
            = CONDITION_SERIALIZERS.register("enable_sewing_crafting", () -> Conditions.EnableSewingCrafting.CODEC);

    public static DeferredItem<ToolBeltItem> BELT = ITEMS.register("belt", () -> new ToolBeltItem(new Item.Properties().stacksTo(1)));
    public static DeferredItem<Item> POUCH = ITEMS.register("pouch", () -> new Item(new Item.Properties()));

    public static DeferredHolder<MenuType<?>, MenuType<BeltSlotMenu>> BELT_SLOT_MENU = MENU_TYPES.register("belt_slot_container", () -> new MenuType<>(BeltSlotMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static DeferredHolder<MenuType<?>, MenuType<BeltContainer>> BELT_MENU = MENU_TYPES.register("belt_container", () -> IMenuTypeExtension.create(BeltContainer::new));

    public static DeferredHolder<IngredientType<?>, IngredientType<BeltIngredient>> BELT_INGREDIENT = INGREDIENT_TYPES.register("belt_upgrade_level", () -> new IngredientType<BeltIngredient>(BeltIngredient.CODEC));

    public ToolBelt(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);
        CONDITION_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::modConfig);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::addItemsToTabs);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(ToolBeltItem::register);

        NeoForge.EVENT_BUS.addListener(this::anvilChange);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigData.COMMON_SPEC);

        if (ModList.get().isLoaded("sewingkit"))
        {
            SewingKitIntegration.init(modEventBus);
        }

        BeltAttachment.register(modEventBus);
    }

    private void addItemsToTabs(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(BELT);
            event.accept(POUCH);
        }
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGen.gatherData(event);
    }

    public void modConfig(ModConfigEvent event)
    {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ConfigData.CLIENT_SPEC)
            ConfigData.refreshClient();
        else if (config.getSpec() == ConfigData.COMMON_SPEC)
            ConfigData.refreshCommon();
        else if (config.getSpec() == ConfigData.SERVER_SPEC)
            ConfigData.refreshServer();
    }

    private void registerPackets(RegisterPayloadHandlerEvent event)
    {
        final IPayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
        registrar.play(SwapItems.ID, SwapItems::new, play -> play.server(SwapItems::handle));
        registrar.play(OpenBeltSlotInventory.ID, OpenBeltSlotInventory::new, play -> play.server(OpenBeltSlotInventory::handle));
        registrar.play(ContainerSlotsHack.ID, ContainerSlotsHack::new, play -> play.server(ContainerSlotsHack::handle));
        registrar.play(BeltContentsChange.ID, BeltContentsChange::new, play -> play.client(BeltContentsChange::handle));
        registrar.play(SyncBeltSlotContents.ID, SyncBeltSlotContents::new, play -> play.client(SyncBeltSlotContents::handle));
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        BeltFinderBeltSlot.initBaubles();

        if (ModList.get().isLoaded("curios"))
        {
            BeltFinderCurios.initCurios();
        }
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(ToolBelt.BELT_MENU.get(), BeltScreen::new);
            MenuScreens.register(ToolBelt.BELT_SLOT_MENU.get(), BeltSlotScreen::new);
        });
    }

    public void anvilChange(AnvilUpdateEvent ev)
    {
        if (!ConfigData.enableAnvilUpgrading)
            return;

        ItemStack left = ev.getLeft();
        ItemStack right = ev.getRight();
        if (left.getCount() <= 0 || left.getItem() != BELT.get())
            return;
        if (right.getCount() <= 0 || right.getItem() != POUCH.get())
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

    public static class DataGen
    {
        public static void gatherData(GatherDataEvent event)
        {
            DataGenerator gen = event.getGenerator();

            gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput(), event.getLookupProvider()));
        }

        private static class Recipes extends RecipeProvider implements IConditionBuilder
        {
            public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup)
            {
                super(output, lookup);
            }

            @Override
            protected void buildRecipes(RecipeOutput consumer)
            {
                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ToolBelt.BELT.get())
                        .pattern("sls")
                        .pattern("l l")
                        .pattern("lil")
                        .define('s', Ingredient.of(Items.STRING))
                        .define('l', Ingredient.of(Items.LEATHER))
                        .define('i', Ingredient.of(Items.IRON_INGOT))
                        .unlockedBy("has_leather", has(itemTag("forge:leather")))
                        .save(consumer.withConditions(
                                new Conditions.EnableNormalCrafting()
                        ));

                SewingRecipeBuilder.begin(RecipeCategory.TOOLS, ToolBelt.BELT.get())
                        .withTool(ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.WOOD.getTier()))
                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                        .addMaterial(Ingredient.of(Items.IRON_INGOT), 1)
                        .addMaterial(Ingredient.of(Items.STRING), 2)
                        .addCriterion("has_leather", has(itemTag("forge:leather")))
                        .save(consumer.withConditions(
                                modLoaded("sewingkit"),
                                new Conditions.EnableSewingCrafting()
                        ), sewingRecipeId(BELT));

                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ToolBelt.POUCH.get())
                        .pattern("sgs")
                        .pattern("l l")
                        .pattern("sls")
                        .define('s', Ingredient.of(Items.STRING))
                        .define('l', Ingredient.of(Items.LEATHER))
                        .define('g', Ingredient.of(Items.GOLD_INGOT))
                        .unlockedBy("has_leather", has(itemTag("forge:leather")))
                        .save(consumer.withConditions(
                                new Conditions.EnableNormalCrafting()
                        ));

                SewingRecipeBuilder.begin(RecipeCategory.TOOLS, ToolBelt.POUCH.get())
                        .withTool(ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.WOOD.getTier()))
                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                        .addMaterial(Ingredient.of(Items.GOLD_INGOT))
                        .addMaterial(Ingredient.of(Items.STRING))
                        .addCriterion("has_leather", has(itemTag("forge:leather")))
                        .save(consumer.withConditions(
                                modLoaded("sewingkit"),
                                new Conditions.EnableSewingCrafting()
                        ), sewingRecipeId(POUCH));

                ToolActionIngredient[] needleTiers = {
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.WOOD.getTier()),
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.BONE.getTier()),
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.IRON.getTier()),
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.IRON.getTier()),
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.DIAMOND.getTier()),
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.DIAMOND.getTier()),
                        ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.NETHERITE.getTier()),
                };
                for (int i = 0; i < 7; i++)
                {

                    var beltId = ToolBelt.BELT.getId();
                    SewingUpgradeRecipeBuilder.begin(ToolBelt.BELT.get(),
                                    compound(
                                            Pair.of("Size", IntTag.valueOf(i + 3))
                                    ))
                            .withTool(needleTiers[i])
                            .addMaterial(BeltIngredient.withLevel(i))
                            .addMaterial(Ingredient.of(POUCH.get()))
                            .addMaterial(Ingredient.of(Items.STRING))
                            .addCriterion("has_leather", has(itemTag("forge:leather")))
                            .save(consumer.withConditions(
                                    modLoaded("sewingkit"),
                                    new Conditions.EnableSewingCrafting()
                            ), new ResourceLocation(beltId.getNamespace(), beltId.getPath() + "_upgrade_" + (i + 1) + "_via_sewing"));
                }
            }

            private ResourceLocation sewingRecipeId(DeferredHolder<?,?> item)
            {
                return sewingRecipeId(item.getId());
            }

            private ResourceLocation sewingRecipeId(ResourceLocation item)
            {
                return  new ResourceLocation(item.getNamespace(), item.getPath() + "_via_sewing");
            }

            public final ItemStack stack(ItemLike item, CompoundTag tag)
            {
                ItemStack stack = new ItemStack(item);
                stack.setTag(tag);
                return stack;
            }

            @SafeVarargs
            public final CompoundTag compound(Pair<String, Tag>... entries)
            {
                CompoundTag tag = new CompoundTag();
                for (Pair<String, Tag> entry : entries)
                {
                    tag.put(entry.getFirst(), entry.getSecond());
                }
                return tag;
            }
        }
    }

    private static TagKey<Item> itemTag(String name)
    {
        return TagKey.create(Registries.ITEM, new ResourceLocation(name));
    }
}
