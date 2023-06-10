package dev.gigaherz.toolbelt;

import com.mojang.datafixers.util.Pair;
import dev.gigaherz.sewingkit.SewingKitMod;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.sewingkit.api.ToolActionIngredient;
import dev.gigaherz.sewingkit.needle.NeedleItem;
import dev.gigaherz.sewingkit.needle.Needles;
import dev.gigaherz.toolbelt.belt.BeltIngredient;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.common.BeltContainer;
import dev.gigaherz.toolbelt.common.BeltScreen;
import dev.gigaherz.toolbelt.common.BeltSlotContainer;
import dev.gigaherz.toolbelt.common.BeltSlotScreen;
import dev.gigaherz.toolbelt.customslots.ExtensionSlotItemCapability;
import dev.gigaherz.toolbelt.integration.SewingKitIntegration;
import dev.gigaherz.toolbelt.integration.SewingUpgradeRecipeBuilder;
import dev.gigaherz.toolbelt.network.*;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.function.Consumer;

@Mod(ToolBelt.MODID)
public class ToolBelt
{
    public static final String MODID = "toolbelt";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static RegistryObject<ToolBeltItem> BELT = ITEMS.register("belt", () -> new ToolBeltItem(new Item.Properties().stacksTo(1)));
    public static RegistryObject<Item> POUCH = ITEMS.register("pouch", () -> new Item(new Item.Properties()));

    public static RegistryObject<RecipeSerializer<?>> SEWING_UGRADE_SERIALIZER = RegistryObject.createOptional(location("sewing_upgrade"), Registries.RECIPE_SERIALIZER, MODID);

    public static RegistryObject<MenuType<BeltSlotContainer>> BELT_SLOT_MENU = MENU_TYPES.register("belt_slot_container", () -> new MenuType<>(BeltSlotContainer::new, FeatureFlags.DEFAULT_FLAGS));
    public static RegistryObject<MenuType<BeltContainer>> BELT_MENU = MENU_TYPES.register("belt_container", () -> IForgeMenuType.create(BeltContainer::new));

    public static ToolBelt instance;

    public static final Logger logger = LogManager.getLogger(MODID);

    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(location("general"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public ToolBelt()
    {
        instance = this;

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::construct);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::modConfig);
        modEventBus.addListener(this::imcEnqueue);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::addItemsToTabs);

        MinecraftForge.EVENT_BUS.addListener(this::anvilChange);

        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigData.COMMON_SPEC);

        if (ModList.get().isLoaded("sewingkit"))
        {
            SewingKitIntegration.init(modEventBus);
        }
    }

    private void addItemsToTabs(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(BELT);
            event.accept(POUCH);
        }
    }

    private void construct(FMLConstructModEvent event)
    {
        event.enqueueWork(() -> {
            CraftingHelper.register(BeltIngredient.NAME, BeltIngredient.Serializer.INSTANCE);
            Conditions.register();
        });
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

    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        ExtensionSlotItemCapability.register(event);
        event.register(BeltExtensionSlot.class);
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        channel.messageBuilder(SwapItems.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(SwapItems::encode).decoder(SwapItems::new).consumerNetworkThread(SwapItems::handle).add();
        channel.messageBuilder(BeltContentsChange.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(BeltContentsChange::encode).decoder(BeltContentsChange::new).consumerNetworkThread(BeltContentsChange::handle).add();
        channel.messageBuilder(OpenBeltSlotInventory.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(OpenBeltSlotInventory::encode).decoder(OpenBeltSlotInventory::new).consumerNetworkThread(OpenBeltSlotInventory::handle).add();
        channel.messageBuilder(ContainerSlotsHack.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(ContainerSlotsHack::encode).decoder(ContainerSlotsHack::new).consumerNetworkThread(ContainerSlotsHack::handle).add();
        channel.messageBuilder(SyncBeltSlotContents.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SyncBeltSlotContents::encode).decoder(SyncBeltSlotContents::new).consumerNetworkThread(SyncBeltSlotContents::handle).add();
        logger.debug("Final message number: " + messageNumber);

        //TODO File configurationFile = event.getSuggestedConfigurationFile();
        //Config.loadConfig(configurationFile);

        BeltExtensionSlot.register();
        BeltFinderBeltSlot.initBaubles();
        CURIOS.addListener(cap -> BeltFinderCurios.initCurios());
    }

    private static final Capability<ICuriosItemHandler> CURIOS = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(ToolBelt.BELT_MENU.get(), BeltScreen::new);
            MenuScreens.register(ToolBelt.BELT_SLOT_MENU.get(), BeltSlotScreen::new);
        });
    }

    private void imcEnqueue(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("belt").size(1).build());
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

            gen.addProvider(event.includeServer(), new Recipes(gen));
        }

        private static class Recipes extends RecipeProvider implements IConditionBuilder
        {
            public Recipes(DataGenerator gen)
            {
                super(gen.getPackOutput());
            }

            @Override
            protected void buildRecipes(Consumer<FinishedRecipe> consumer)
            {
                ResourceLocation beltId = ToolBelt.BELT.getId();
                ConditionalRecipe.builder()
                        .addCondition(new Conditions.EnableNormalCrafting())
                        .addRecipe(
                                p_176499_ -> ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ToolBelt.BELT.get())
                                        .pattern("sls")
                                        .pattern("l l")
                                        .pattern("lil")
                                        .define('s', Ingredient.of(Items.STRING))
                                        .define('l', Ingredient.of(Items.LEATHER))
                                        .define('i', Ingredient.of(Items.IRON_INGOT))
                                        .unlockedBy("has_leather", has(itemTag("forge:leather"))).save(p_176499_)
                        )
                        .generateAdvancement()
                        .build(consumer, beltId);

                ConditionalRecipe.builder()
                        .addCondition(modLoaded("sewingkit"))
                        .addCondition(new Conditions.EnableSewingCrafting())
                        .addRecipe(
                                consumerIn -> SewingRecipeBuilder.begin(RecipeCategory.TOOLS, ToolBelt.BELT.get())
                                        .withTool(ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.WOOD.getTier()))
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                                        .addMaterial(Ingredient.of(Items.IRON_INGOT), 1)
                                        .addMaterial(Ingredient.of(Items.STRING), 2)
                                        .addCriterion("has_leather", has(itemTag("forge:leather"))).save(consumerIn, RecipeBuilder.getDefaultRecipeId(ToolBelt.BELT.get()))
                        )
                        .generateAdvancement()
                        .build(consumer, new ResourceLocation(beltId.getNamespace(), beltId.getPath() + "_via_sewing"));

                ResourceLocation pouchId = ToolBelt.POUCH.getId();
                ConditionalRecipe.builder()
                        .addCondition(new Conditions.EnableNormalCrafting())
                        .addRecipe(
                                p_176499_ -> ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ToolBelt.POUCH.get())
                                        .pattern("sgs")
                                        .pattern("l l")
                                        .pattern("sls")
                                        .define('s', Ingredient.of(Items.STRING))
                                        .define('l', Ingredient.of(Items.LEATHER))
                                        .define('g', Ingredient.of(Items.GOLD_INGOT))
                                        .unlockedBy("has_leather", has(itemTag("forge:leather"))).save(p_176499_)
                        )
                        .generateAdvancement()
                        .build(consumer, pouchId);

                ConditionalRecipe.builder()
                        .addCondition(modLoaded("sewingkit"))
                        .addCondition(new Conditions.EnableSewingCrafting())
                        .addRecipe(
                                consumerIn -> SewingRecipeBuilder.begin(RecipeCategory.TOOLS, ToolBelt.POUCH.get())
                                        .withTool(ToolActionIngredient.fromTool(NeedleItem.SEW, Needles.WOOD.getTier()))
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                                        .addMaterial(Ingredient.of(Items.GOLD_INGOT))
                                        .addMaterial(Ingredient.of(Items.STRING))
                                        .addCriterion("has_leather", has(itemTag("forge:leather"))).save(consumerIn, RecipeBuilder.getDefaultRecipeId(ToolBelt.POUCH.get()))
                        )
                        .generateAdvancement()
                        .build(consumer, new ResourceLocation(pouchId.getNamespace(), pouchId.getPath() + "_via_sewing"));
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
                    final int index = i;
                    ConditionalRecipe.builder()
                            .addCondition(modLoaded("sewingkit"))
                            .addCondition(new Conditions.EnableSewingCrafting())
                            .addRecipe(
                                    consumerIn -> SewingUpgradeRecipeBuilder.begin(ToolBelt.BELT.get(),
                                                    compound(
                                                            Pair.of("Size", IntTag.valueOf(index + 3))
                                                    ))
                                            .withTool(needleTiers[index])
                                            .addMaterial(BeltIngredient.withLevel(index))
                                            .addMaterial(Ingredient.of(POUCH.get()))
                                            .addMaterial(Ingredient.of(Items.STRING))
                                            .addCriterion("has_leather", has(itemTag("forge:leather"))).save(consumerIn, RecipeBuilder.getDefaultRecipeId(ToolBelt.BELT.get()))
                            )
                            .generateAdvancement()
                            .build(consumer, new ResourceLocation(pouchId.getNamespace(), pouchId.getPath() + "_upgrade_" + (i + 1) + "_via_sewing"));
                }
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
