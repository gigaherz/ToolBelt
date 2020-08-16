package gigaherz.toolbelt.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ConfigData;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.client.radial.*;
import gigaherz.toolbelt.network.SwapItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RadialMenuScreen extends Screen
{
    private final BeltFinder.BeltGetter getter;
    private ItemStack stackEquipped;
    private IItemHandler inventory;

    private boolean keyCycleBeforeL = false;
    private boolean keyCycleBeforeR = false;

    private boolean needsRecheckStacks = true;
    private final List<ItemStackRadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final TextRadialMenuItem insertMenuItem;
    private final GenericRadialMenu menu;

    private ItemRenderer getItemRenderer()
    {
        return itemRenderer;
    }

    public RadialMenuScreen(BeltFinder.BeltGetter getter)
    {
        super(new StringTextComponent("RADIAL MENU"));

        this.getter = getter;
        this.stackEquipped = getter.getBelt();
        inventory = stackEquipped.getCount() > 0 ? stackEquipped.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(null) : null;
        menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost()
        {
            @Override
            public void renderTooltip(MatrixStack matrixStack, ItemStack stack, int mouseX, int mouseY)
            {
                RadialMenuScreen.this.renderTooltip(matrixStack, stack, mouseX, mouseY);
            }

            @Override
            public Screen getScreen()
            {
                return RadialMenuScreen.this;
            }

            @Override
            public FontRenderer getFontRenderer()
            {
                return font;
            }

            @Override
            public ItemRenderer getItemRenderer()
            {
                return RadialMenuScreen.this.getItemRenderer();
            }
        })
        {
            @Override
            public void onClickOutside()
            {
                close();
            }
        };
        insertMenuItem = new TextRadialMenuItem(menu, new TranslationTextComponent("text.toolbelt.insert"))
        {
            @Override
            public boolean onClick()
            {
                return RadialMenuScreen.this.trySwap(-1, ItemStack.EMPTY);
            }
        };
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;

        if (Minecraft.getInstance().currentScreen instanceof RadialMenuScreen)
        {
            event.setCanceled(true);
        }
    }

    @Override // removed
    public void onClose()
    {
        super.onClose();
        ClientEvents.wipeOpen();
    }

    @Override // tick
    public void tick()
    {
        super.tick();

        menu.tick();

        if (menu.isClosed())
        {
            Minecraft.getInstance().displayGuiScreen(null);
            ClientEvents.wipeOpen();
        }
        if (!menu.isReady() || inventory == null)
        {
            return;
        }

        ItemStack inHand = minecraft.player.getHeldItemMainhand();
        if (!ConfigData.isItemStackAllowed(inHand))
        {
            inventory = null;
        }
        else
        {
            ItemStack stack = getter.getBelt();
            if (stack.getCount() <= 0)
            {
                inventory = null;
                stackEquipped = null;
            }
            // Reference comparison intended
            else if (stackEquipped != stack)
            {
                stackEquipped = stack;
                inventory = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(() -> new RuntimeException("No inventory?"));
                needsRecheckStacks = true;
            }
        }

        if (inventory == null)
        {
            Minecraft.getInstance().displayGuiScreen(null);
        }
        else if (!InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), ClientEvents.OPEN_TOOL_MENU_KEYBIND.getKey().getKeyCode()))
        {
            if (ConfigData.releaseToSwap)
            {
                processClick(false);
            }
            else
            {
                menu.close();
            }
        }
    }

    @Override // mouseReleased
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_)
    {
        processClick(true);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    protected void processClick(boolean triggeredByMouse)
    {
        menu.clickItem();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (inventory == null)
            return;

        ItemStack inHand = minecraft.player.getHeldItemMainhand();
        if (!ConfigData.isItemStackAllowed(inHand))
            return;

        if (needsRecheckStacks)
        {
            cachedMenuItems.clear();
            for (int i = 0; i < inventory.getSlots(); i++)
            {
                ItemStack inSlot = inventory.getStackInSlot(i);
                ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(menu, i, inSlot, new TranslationTextComponent("text.toolbelt.empty"))
                {
                    @Override
                    public boolean onClick()
                    {
                        return RadialMenuScreen.this.trySwap(getSlot(), getStack());
                    }
                };
                item.setVisible(inSlot.getCount() > 0 || ConfigData.displayEmptySlots);
                if (inHand.getCount() > 0)
                {
                    if (inSlot.getCount() > 0)
                        item.setCentralText(new TranslationTextComponent("text.toolbelt.swap"));
                    else
                        item.setCentralText(new TranslationTextComponent("text.toolbelt.insert"));
                }
                else
                {
                    if (inSlot.getCount() > 0)
                        item.setCentralText(new TranslationTextComponent("text.toolbelt.extract"));
                    else
                        item.setCentralText(new TranslationTextComponent("text.toolbelt.empty"));
                }
                cachedMenuItems.add(item);
            }

            menu.clear();
            menu.addAll(cachedMenuItems);
            menu.add(insertMenuItem);

            needsRecheckStacks = false;
        }

        boolean hasAddButton = false;
        if (!ConfigData.displayEmptySlots && !cachedMenuItems.stream().allMatch(RadialMenuItem::isVisible) && inHand.getCount() > 0)
        {
            hasAddButton = true;
        }
        insertMenuItem.setVisible(hasAddButton);

        if (cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible))
        {
            menu.setCentralText(new TranslationTextComponent("text.toolbelt.empty"));
        }
        else
        {
            menu.setCentralText(null);
        }

        checkCycleKeybinds();

        menu.draw(matrixStack, partialTicks, mouseX, mouseY);
    }

    private boolean trySwap(int slotNumber, ItemStack itemMouseOver)
    {
        ItemStack inHand = minecraft.player.getHeldItemMainhand();
        if (!ConfigData.isItemStackAllowed(inHand))
            return false;

        if (inHand.getCount() > 0 || itemMouseOver.getCount() > 0)
        {
            SwapItems.swapItem(slotNumber, minecraft.player);
            ToolBelt.channel.sendToServer(new SwapItems(slotNumber));
        }

        menu.close();
        return true;
    }

    private void checkCycleKeybinds()
    {
        if (ClientEvents.isKeyDown(ClientEvents.CYCLE_TOOL_MENU_LEFT_KEYBIND))
        {
            if (!keyCycleBeforeL)
            {
                menu.cyclePrevious();
            }
            keyCycleBeforeL = true;
        }
        else
        {
            keyCycleBeforeL = false;
        }

        if (ClientEvents.isKeyDown(ClientEvents.CYCLE_TOOL_MENU_RIGHT_KEYBIND))
        {
            if (!keyCycleBeforeR)
            {
                menu.cycleNext();
            }
            keyCycleBeforeR = true;
        }
        else
        {
            keyCycleBeforeR = false;
        }
    }

    @Override // isPauseScreen
    public boolean isPauseScreen()
    {
        return false;
    }
}
