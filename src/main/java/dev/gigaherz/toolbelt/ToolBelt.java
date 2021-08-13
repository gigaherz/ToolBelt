package dev.gigaherz.toolbelt;

import com.mojang.datafixers.util.Pair;
import dev.gigaherz.sewingkit.SewingKitMod;
import dev.gigaherz.sewingkit.api.SewingRecipeBuilder;
import dev.gigaherz.sewingkit.api.ToolIngredient;
import dev.gigaherz.sewingkit.needle.NeedleItem;
import dev.gigaherz.sewingkit.needle.Needles;
import dev.gigaherz.toolbelt.belt.BeltIngredient;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.client.ClientEvents;
import dev.gigaherz.toolbelt.common.BeltContainer;
import dev.gigaherz.toolbelt.common.BeltScreen;
import dev.gigaherz.toolbelt.common.BeltSlotContainer;
import dev.gigaherz.toolbelt.common.BeltSlotScreen;
import dev.gigaherz.toolbelt.customslots.ExtensionSlotItemCapability;
import dev.gigaherz.toolbelt.integration.SewingKitIntegration;
import dev.gigaherz.toolbelt.integration.SewingUpgradeRecipe;
import dev.gigaherz.toolbelt.integration.SewingUpgradeRecipeBuilder;
import dev.gigaherz.toolbelt.network.*;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import java.util.Objects;
import java.util.function.Consumer;

@Mod(ToolBelt.MODID)
public class ToolBelt
{
    public static final String MODID = "toolbelt";

    @ObjectHolder("toolbelt:belt")
    public static ToolBeltItem BELT;

    @ObjectHolder("toolbelt:pouch")
    public static Item POUCH;

    @ObjectHolder("toolbelt:sewing_upgrade")
    public static RecipeSerializer<?> SEWING_UGRADE_SERIALIZER = null;

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

        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipes);
        modEventBus.addGenericListener(MenuType.class, this::registerContainers);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::modConfig);
        modEventBus.addListener(this::imcEnqueue);
        modEventBus.addListener(this::gatherData);

        MinecraftForge.EVENT_BUS.addListener(this::anvilChange);

        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigData.COMMON_SPEC);

        if (ModList.get().isLoaded("sewingkit"))
        {
            SewingKitIntegration.init();
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
        else if (config.getSpec() == ConfigData.SERVER_SPEC)
            ConfigData.refreshServer();
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new ToolBeltItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS)).setRegistryName("belt"),
                new Item(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)).setRegistryName("pouch")
        );
    }

    private void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event)
    {
        CraftingHelper.register(BeltIngredient.NAME, BeltIngredient.Serializer.INSTANCE);
    }

    public void registerContainers(RegistryEvent.Register<MenuType<?>> event)
    {
        event.getRegistry().registerAll(
                new MenuType<>(BeltSlotContainer::new).setRegistryName("belt_slot_container"),
                IForgeContainerType.create(BeltContainer::new).setRegistryName("belt_container")
        );

        // FIXME: Move elsewhere:
        Conditions.register();
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        channel.messageBuilder(SwapItems.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(SwapItems::encode).decoder(SwapItems::new).consumer(SwapItems::handle).add();
        channel.messageBuilder(BeltContentsChange.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(BeltContentsChange::encode).decoder(BeltContentsChange::new).consumer(BeltContentsChange::handle).add();
        channel.messageBuilder(OpenBeltSlotInventory.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(OpenBeltSlotInventory::encode).decoder(OpenBeltSlotInventory::new).consumer(OpenBeltSlotInventory::handle).add();
        channel.messageBuilder(ContainerSlotsHack.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(ContainerSlotsHack::encode).decoder(ContainerSlotsHack::new).consumer(ContainerSlotsHack::handle).add();
        channel.messageBuilder(SyncBeltSlotContents.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SyncBeltSlotContents::encode).decoder(SyncBeltSlotContents::new).consumer(SyncBeltSlotContents::handle).add();
        logger.debug("Final message number: " + messageNumber);

        //TODO File configurationFile = event.getSuggestedConfigurationFile();
        //Config.loadConfig(configurationFile);

        ExtensionSlotItemCapability.register();
        BeltExtensionSlot.register();
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(BeltContainer.TYPE, BeltScreen::new);
            MenuScreens.register(BeltSlotContainer.TYPE, BeltSlotScreen::new);
        });
    }

    private void imcEnqueue(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("belt").size(1).build());
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) ClientEvents.initKeybinds();
        });
    }

    public void anvilChange(AnvilUpdateEvent ev)
    {
        if (!ConfigData.enableAnvilUpgrading)
            return;

        ItemStack left = ev.getLeft();
        ItemStack right = ev.getRight();
        if (left.getCount() <= 0 || left.getItem() != BELT)
            return;
        if (right.getCount() <= 0 || right.getItem() != POUCH)
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

            if (event.includeClient())
            {
                //gen.addProvider(new SewingKitDataGen.Lang(gen));
                // Let blockstate provider see generated item models by passing its existing file helper
                //ItemModelProvider itemModels = new SewingKitDataGen.ItemModels(gen, event.getExistingFileHelper());
                //gen.addProvider(itemModels);
                //gen.addProvider(new BlockStates(gen, itemModels.existingFileHelper));
            }
            if (event.includeServer())
            {

                //BlockTags blockTags = new BlockTags(gen);
                //gen.addProvider(blockTags);
                //gen.addProvider(new ItemTags(gen, blockTags));
                gen.addProvider(new Recipes(gen));
                //gen.addProvider(new LootTables(gen));
            }
        }

        private static class Recipes extends RecipeProvider implements IConditionBuilder
        {
            public Recipes(DataGenerator gen)
            {
                super(gen);
            }

            @Override
            protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
            {
                ResourceLocation beltId = Objects.requireNonNull(ToolBelt.BELT.getRegistryName());
                ConditionalRecipe.builder()
                        .addCondition(new Conditions.EnableNormalCrafting())
                        .addRecipe(
                                ShapedRecipeBuilder.shaped(ToolBelt.BELT)
                                        .pattern("sls")
                                        .pattern("l l")
                                        .pattern("lil")
                                        .define('s', Ingredient.of(Items.STRING))
                                        .define('l', Ingredient.of(Items.LEATHER))
                                        .define('i', Ingredient.of(Items.IRON_INGOT))
                                        .unlockedBy("has_leather", has(ItemTags.bind("forge:leather")))
                                        ::save
                        )
                        .generateAdvancement()
                        .build(consumer, beltId);

                ConditionalRecipe.builder()
                        .addCondition(modLoaded("sewingkit"))
                        .addCondition(new Conditions.EnableSewingCrafting())
                        .addRecipe(
                                SewingRecipeBuilder.begin(ToolBelt.BELT)
                                        .withTool(ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.WOOD.getLevel()))
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                                        .addMaterial(Ingredient.of(Items.IRON_INGOT), 1)
                                        .addMaterial(Ingredient.of(Items.STRING), 2)
                                        .addCriterion("has_leather", has(ItemTags.bind("forge:leather")))
                                        ::build
                        )
                        .generateAdvancement()
                        .build(consumer, new ResourceLocation(beltId.getNamespace(), beltId.getPath() + "_via_sewing"));

                ResourceLocation pouchId = Objects.requireNonNull(ToolBelt.POUCH.getRegistryName());
                ConditionalRecipe.builder()
                        .addCondition(new Conditions.EnableNormalCrafting())
                        .addRecipe(
                                ShapedRecipeBuilder.shaped(ToolBelt.POUCH)
                                        .pattern("sgs")
                                        .pattern("l l")
                                        .pattern("sls")
                                        .define('s', Ingredient.of(Items.STRING))
                                        .define('l', Ingredient.of(Items.LEATHER))
                                        .define('g', Ingredient.of(Items.GOLD_INGOT))
                                        .unlockedBy("has_leather", has(ItemTags.bind("forge:leather")))
                                        ::save
                        )
                        .generateAdvancement()
                        .build(consumer, pouchId);

                ConditionalRecipe.builder()
                        .addCondition(modLoaded("sewingkit"))
                        .addCondition(new Conditions.EnableSewingCrafting())
                        .addRecipe(
                                SewingRecipeBuilder.begin(ToolBelt.POUCH)
                                        .withTool(ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.WOOD.getLevel()))
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_STRIP.get()), 2)
                                        .addMaterial(Ingredient.of(SewingKitMod.LEATHER_SHEET.get()), 3)
                                        .addMaterial(Ingredient.of(Items.GOLD_INGOT))
                                        .addMaterial(Ingredient.of(Items.STRING))
                                        .addCriterion("has_leather", has(ItemTags.bind("forge:leather")))
                                        ::build
                        )
                        .generateAdvancement()
                        .build(consumer, new ResourceLocation(pouchId.getNamespace(), pouchId.getPath() + "_via_sewing"));
                ToolIngredient needleTiers[] = {
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.WOOD.getLevel()),
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.BONE.getLevel()),
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.IRON.getLevel()),
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.IRON.getLevel()),
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.DIAMOND.getLevel()),
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.DIAMOND.getLevel()),
                        ToolIngredient.fromTool(NeedleItem.SEWING_NEEDLE, Needles.NETHERITE.getLevel()),
                };
                for (int i = 0; i < 7; i++)
                {
                    ConditionalRecipe.builder()
                            .addCondition(modLoaded("sewingkit"))
                            .addCondition(new Conditions.EnableSewingCrafting())
                            .addRecipe(
                                    SewingUpgradeRecipeBuilder.begin(ToolBelt.BELT,
                                            compound(
                                                    Pair.of("Size", IntTag.valueOf(i + 3))
                                            ))
                                            .withTool(needleTiers[i])
                                            .addMaterial(BeltIngredient.withLevel(i))
                                            .addMaterial(Ingredient.of(POUCH))
                                            .addMaterial(Ingredient.of(Items.STRING))
                                            .addCriterion("has_leather", has(ItemTags.bind("forge:leather")))
                                            ::build
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
}
