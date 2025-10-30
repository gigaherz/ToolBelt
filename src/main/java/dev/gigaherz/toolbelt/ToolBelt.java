package dev.gigaherz.toolbelt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.gigaherz.sewingkit.SewingKitMod;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.toolbelt.belt.BeltIngredient;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.client.HasCustomColor;
import dev.gigaherz.toolbelt.common.BeltContainer;
import dev.gigaherz.toolbelt.common.BeltScreen;
import dev.gigaherz.toolbelt.integration.SewingKitIntegration;
import dev.gigaherz.toolbelt.integration.SewingUpgradeRecipe;
import dev.gigaherz.toolbelt.network.*;
import dev.gigaherz.toolbelt.slot.BeltAttachment;
import dev.gigaherz.toolbelt.slot.BeltSlotMenu;
import dev.gigaherz.toolbelt.slot.BeltSlotScreen;
import net.minecraft.Util;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod(ToolBelt.MODID)
public class ToolBelt
{
    public static final String MODID = "toolbelt";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MODID);

    private static final DeferredRegister<MapCodec<? extends ICondition>>
            CONDITION_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, ToolBelt.MODID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<Conditions.EnableNormalCrafting>>
            ENABLE_NORMAL = CONDITION_SERIALIZERS.register("enable_normal_crafting", () -> Conditions.EnableNormalCrafting.CODEC);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<Conditions.EnableSewingCrafting>>
            ENABLE_SEWING = CONDITION_SERIALIZERS.register("enable_sewing_crafting", () -> Conditions.EnableSewingCrafting.CODEC);

    public static DeferredItem<ToolBeltItem>
            BELT = ITEMS.registerItem("belt", props -> new ToolBeltItem(props.stacksTo(1)));
    public static DeferredItem<Item>
            POUCH = ITEMS.registerSimpleItem("pouch");

    public static DeferredHolder<MenuType<?>, MenuType<BeltSlotMenu>>
            BELT_SLOT_MENU = MENU_TYPES.register("belt_slot_container", () -> new MenuType<>(BeltSlotMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static DeferredHolder<MenuType<?>, MenuType<BeltContainer>>
            BELT_MENU = MENU_TYPES.register("belt_container", () -> IMenuTypeExtension.create(BeltContainer::new));

    public static DeferredHolder<IngredientType<?>, IngredientType<BeltIngredient>>
            BELT_INGREDIENT = INGREDIENT_TYPES.register("belt_upgrade_level", () -> new IngredientType<BeltIngredient>(BeltIngredient.CODEC));

    public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BELT_SIZE = DATA_COMPONENT_TYPES.register("belt_size", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public ToolBelt(ModContainer container, IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);
        CONDITION_SERIALIZERS.register(modEventBus);
        DATA_COMPONENT_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::modConfigLoad);
        modEventBus.addListener(this::modConfigReload);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::addItemsToTabs);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(ToolBeltItem::register);

        NeoForge.EVENT_BUS.addListener(this::anvilChange);

        container.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, ConfigData.COMMON_SPEC);

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

    public void gatherData(GatherDataEvent.Client event)
    {
        DataGen.gatherData(event);
    }

    public void modConfigLoad(ModConfigEvent.Loading event)
    {
        reloadConfigs(event.getConfig());
    }

    public void modConfigReload(ModConfigEvent.Reloading event)
    {
        reloadConfigs(event.getConfig());
    }

    private void reloadConfigs(ModConfig config)
    {
        if (config.getSpec() == ConfigData.CLIENT_SPEC)
            ConfigData.refreshClient();
        else if (config.getSpec() == ConfigData.COMMON_SPEC)
            ConfigData.refreshCommon();
        else if (config.getSpec() == ConfigData.SERVER_SPEC)
            ConfigData.refreshServer();
    }

    private void registerPackets(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
        registrar.playToServer(SwapItems.TYPE, SwapItems.STREAM_CODEC, SwapItems::handle);
        registrar.playToServer(OpenBeltSlotInventory.TYPE, OpenBeltSlotInventory.STREAM_CODEC, OpenBeltSlotInventory::handle);
        registrar.playToServer(ContainerSlotsHack.TYPE, ContainerSlotsHack.STREAM_CODEC, ContainerSlotsHack::handle);
        registrar.playToClient(BeltContentsChange.TYPE, BeltContentsChange.STREAM_CODEC, BeltContentsChange::handle);
        registrar.playToClient(SyncBeltSlotContents.TYPE, SyncBeltSlotContents.STREAM_CODEC, SyncBeltSlotContents::handle);
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        BeltFinderBeltSlot.initBeltSlot();

        /*if (ModList.get().isLoaded("curios"))
        {
            BeltFinderCurios.initCurios();
        }*/
    }

    @EventBusSubscriber(value= Dist.CLIENT, modid = MODID)
    public static class ClientModBusEvents
    {
        @SubscribeEvent
        public static void menuScreens(RegisterMenuScreensEvent event)
        {
            event.register(ToolBelt.BELT_MENU.get(), BeltScreen::new);
            event.register(ToolBelt.BELT_SLOT_MENU.get(), BeltSlotScreen::new);
        }
    }


    public static final int[] XP_COSTS = {3, 5, 8, 12, 15, 20, 30};

    public static int getUpgradeXP(ItemStack stack)
    {
        int slots = ToolBeltItem.getBeltSize(stack);

        if (slots >= 9)
            return -1;

        if (slots < 2)
            return 1;

        return XP_COSTS[slots - 2];
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
        int cost = getUpgradeXP(left);
        if (cost < 0)
        {
            ev.setCanceled(true);
            return;
        }
        ev.setXpCost(cost);
        ev.setMaterialCost(1);

        ev.setOutput(ToolBeltItem.makeUpgradedStack(left));
    }

    public static ResourceLocation location(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static class DataGen
    {
        public static void gatherData(GatherDataEvent.Client event)
        {
            DataGenerator gen = event.getGenerator();

            gen.addProvider(true, new Recipes(gen.getPackOutput(), event.getLookupProvider()));
            gen.addProvider(true, new ModelsAndClientItems(gen.getPackOutput()));
            gen.addProvider(true, new ItemTagGen(gen.getPackOutput()));
            //gen.addProvider(true, new MyCuriosDataProvider(gen, event));
        }

        private static class ModelsAndClientItems extends ModelProvider
        {
            public ModelsAndClientItems(PackOutput output)
            {
                super(output, MODID);
            }

            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels)
            {
                itemModels.generateLayeredItem(BELT.get(), location("item/belt"), location("item/belt_buckle"));
                itemModels.generateLayeredItem(location("item/dyed_belt"), location("item/dyed_belt"), location("item/belt_buckle"));
                itemModels.itemModelOutput.accept(BELT.get(),
                        ItemModelUtils.conditional(
                                HasCustomColor.INSTANCE,
                                ItemModelUtils.tintedModel(location("item/dyed_belt"), new Dye(-1)),
                                ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(BELT.get()))
                        ));

                itemModels.createFlatItemModel(POUCH.get(), ModelTemplates.FLAT_ITEM);
                itemModels.itemModelOutput.accept(POUCH.get(),
                        ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(POUCH.get())));
            }
        }

        private static class ItemTagGen extends IntrinsicHolderTagsProvider<Item>
        {
            public ItemTagGen(PackOutput packOutput)
            {
                super(packOutput, Registries.ITEM, CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor()),
                        (item) -> BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(), SewingKitMod.MODID);
            }

            @Override
            protected void addTags(HolderLookup.Provider lookup)
            {
                tag(ItemTags.DYEABLE)
                        .add(BELT.get());
                /*tag(CuriosTags.BELT)
                        .add(BELT.get());*/
            }
        }

        private static class Recipes extends RecipeProvider.Runner
        {
            public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
            {
                super(output, lookupProvider);
            }

            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookup, RecipeOutput output)
            {
                return new VanillaRecipeProvider(lookup, output)
                {

                    @Override
                    protected void buildRecipes()
                    {
                        var items = lookup.lookupOrThrow(Registries.ITEM);

                        shaped(RecipeCategory.TOOLS, ToolBelt.BELT.get())
                                .pattern("sls")
                                .pattern("l l")
                                .pattern("lil")
                                .define('s', Ingredient.of(Items.STRING))
                                .define('l', Ingredient.of(Items.LEATHER))
                                .define('i', Ingredient.of(Items.IRON_INGOT))
                                .unlockedBy("has_leather", has(Tags.Items.LEATHERS))
                                .save(output.withConditions(
                                        new Conditions.EnableNormalCrafting()
                                ));

                        SewingRecipeBuilder.begin(items, RecipeCategory.TOOLS, ToolBelt.BELT.get())
                                .withTool(SewingKitMod.WOOD_OR_HIGHER)
                                .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP), 2)
                                .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET), 3)
                                .addMaterial(Ingredient.of(Items.IRON_INGOT), 1)
                                .addMaterial(Ingredient.of(Items.STRING), 2)
                                .addCriterion("has_leather", has(Tags.Items.LEATHERS))
                                .save(output.withConditions(
                                        new ModLoadedCondition("sewingkit"),
                                        new Conditions.EnableSewingCrafting()
                                ), sewingRecipeId(BELT));

                        shaped(RecipeCategory.TOOLS, ToolBelt.POUCH.get())
                                .pattern("sgs")
                                .pattern("l l")
                                .pattern("sls")
                                .define('s', Ingredient.of(Items.STRING))
                                .define('l', Ingredient.of(Items.LEATHER))
                                .define('g', Ingredient.of(Items.GOLD_INGOT))
                                .unlockedBy("has_leather", has(Tags.Items.LEATHERS))
                                .save(output.withConditions(
                                        new Conditions.EnableNormalCrafting()
                                ));

                        SewingRecipeBuilder.begin(items, RecipeCategory.TOOLS, ToolBelt.POUCH.get())
                                .withTool(SewingKitMod.WOOD_OR_HIGHER)
                                .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                                .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                                .addMaterial(Ingredient.of(Items.GOLD_INGOT))
                                .addMaterial(Ingredient.of(Items.STRING))
                                .addCriterion("has_leather", has(Tags.Items.LEATHERS))
                                .save(output.withConditions(
                                        new ModLoadedCondition("sewingkit"),
                                        new Conditions.EnableSewingCrafting()
                                ), sewingRecipeId(POUCH));

                        List<TagKey<Item>> needleTiers = List.of(
                                SewingKitMod.WOOD_OR_HIGHER,
                                SewingKitMod.BONE_OR_HIGHER,
                                SewingKitMod.IRON_OR_HIGHER,
                                SewingKitMod.IRON_OR_HIGHER,
                                SewingKitMod.DIAMOND_OR_HIGHER,
                                SewingKitMod.DIAMOND_OR_HIGHER,
                                SewingKitMod.NETHERITE_OR_HIGHER
                        );
                        for (int i = 2; i < 9; i++)
                        {
                            var beltId = ToolBelt.BELT.getId();
                            SewingUpgradeRecipe.builder(items, ToolBeltItem.of(i + 1))
                                    .withTool(needleTiers.get(i - 2))
                                    .addMaterial(BeltIngredient.withLevel(i).toVanilla())
                                    .addMaterial(Ingredient.of(POUCH.get()))
                                    .addMaterial(Ingredient.of(Items.STRING))
                                    .setGroup("toolbelt_belt_upgrade")
                                    .addCriterion("has_leather", has(Tags.Items.LEATHERS))
                                    .save(output.withConditions(
                                            new ModLoadedCondition("sewingkit"),
                                            new Conditions.EnableSewingCrafting()
                                    ), ResourceLocation.fromNamespaceAndPath(beltId.getNamespace(), beltId.getPath() + "_upgrade_" + (i - 1) + "_via_sewing"));
                        }
                    }

                    private ResourceLocation sewingRecipeId(DeferredHolder<?, ?> item)
                    {
                        return sewingRecipeId(item.getId());
                    }

                    private ResourceLocation sewingRecipeId(ResourceLocation item)
                    {
                        return ResourceLocation.fromNamespaceAndPath(item.getNamespace(), item.getPath() + "_via_sewing");
                    }
                };
            }

            @Override
            public String getName()
            {
                return "Recipes";
            }
        }

        /*private static class MyCuriosDataProvider extends CuriosDataProvider
        {
            public MyCuriosDataProvider(DataGenerator gen, GatherDataEvent.Client event)
            {
                super(ToolBelt.MODID, gen.getPackOutput(), event.getLookupProvider());
            }

            @Override
            public void generate(HolderLookup.Provider registries)
            {
                this.createEntities("curio_slots")
                        .addPlayer().addSlots("belt");
                this.createSlot("belt").size(1);
            }
        }*/
    }
}
