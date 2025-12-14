package dev.gigaherz.toolbelt.client.radial;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import dev.gigaherz.toolbelt.ConfigData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class GenericRadialMenu
{
    public static final float OPEN_ANIMATION_LENGTH = 2.5f;

    public final IRadialMenuHost host;
    private final List<RadialMenuItem> items = Lists.newArrayList();
    private final List<RadialMenuItem> visibleItems = Lists.newArrayList();
    private final Minecraft minecraft;
    public int backgroundColor = 0x3F000000;
    public int backgroundColorHover = 0x3FFFFFFF;

    public enum State
    {
        INITIALIZING,
        OPENING,
        NORMAL,
        CLOSING,
        CLOSED
    }

    private State state = State.INITIALIZING;
    public double startAnimation;
    public float animProgress;
    public float radiusIn;
    public float radiusOut;
    public float itemRadius;
    public float animTop;

    private Component centralText;

    public GenericRadialMenu(Minecraft minecraft, IRadialMenuHost host)
    {
        this.minecraft = minecraft;
        this.host = host;
    }

    public void setCentralText(@Nullable Component centralText)
    {
        this.centralText = centralText;
    }

    public Component getCentralText()
    {
        return centralText;
    }

    public int getHovered()
    {
        for (int i = 0; i < visibleItems.size(); i++)
        {
            if (visibleItems.get(i).isHovered())
                return i;
        }
        return -1;
    }

    @Nullable
    public RadialMenuItem getHoveredItem()
    {
        for (RadialMenuItem item : visibleItems)
        {
            if (item.isHovered())
                return item;
        }
        return null;
    }

    public void setHovered(int which)
    {
        for (int i = 0; i < visibleItems.size(); i++)
        {
            visibleItems.get(i).setHovered(i == which);
        }
    }

    public int getVisibleItemCount()
    {
        return visibleItems.size();
    }

    public void clickItem()
    {
        switch (state)
        {
            case NORMAL:
                RadialMenuItem item = getHoveredItem();
                if (item != null)
                {
                    item.onClick();
                    return;
                }
                break;
            default:
                break;
        }
        onClickOutside();
    }

    public void onClickOutside()
    {
        // to be implemented by users
    }

    public boolean isClosed()
    {
        return state == State.CLOSED;
    }

    public boolean isReady()
    {
        return state == State.NORMAL;
    }

    public void visibilityChanged(RadialMenuItem item)
    {
        visibleItems.clear();
        for (RadialMenuItem radialMenuItem : items)
        {
            if (radialMenuItem.isVisible())
            {
                visibleItems.add(radialMenuItem);
            }
        }
    }

    public void add(RadialMenuItem item)
    {
        items.add(item);
        if (item.isVisible())
        {
            visibleItems.add(item);
        }
    }

    public void addAll(Collection<? extends RadialMenuItem> cachedMenuItems)
    {
        items.addAll(cachedMenuItems);
        for (RadialMenuItem cachedMenuItem : cachedMenuItems)
        {
            if (cachedMenuItem.isVisible())
            {
                visibleItems.add(cachedMenuItem);
            }
        }
    }

    public void clear()
    {
        items.clear();
        visibleItems.clear();
    }

    public void close()
    {
        state = State.CLOSING;
        startAnimation = minecraft.level.getGameTime() + (double) minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        animProgress = 1.0f;
        setHovered(-1);
    }

    public void tick()
    {
        if (state == State.INITIALIZING)
        {
            startAnimation = minecraft.level.getGameTime() + (double) minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            state = State.OPENING;
            animProgress = 0;
        }

        //updateAnimationState(minecraft.getRenderPartialTicks());
    }

    public void draw(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        updateAnimationState(partialTicks);

        if (isClosed())
            return;

        if (isReady())
            processMouse(mouseX, mouseY);

        Screen owner = host.getScreen();
        Font font = host.getFontRenderer();

        boolean animated = state == State.OPENING || state == State.CLOSING;
        radiusIn = animated ? Math.max(0.1f, 30 * animProgress) : 30;
        radiusOut = radiusIn * 2;
        itemRadius = (radiusIn + radiusOut) * 0.5f;
        animTop = animated ? (1 - animProgress) * owner.height / 2.0f : 0;

        int x = owner.width / 2;
        int y = owner.height / 2;

        var poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(0, animTop);

        drawBackground(graphics, x, y, radiusIn, radiusOut);

        poseStack.popMatrix();

        if (isReady())
        {
            poseStack.pushMatrix();
            drawItems(graphics, x, y, owner.width, owner.height, font);
            poseStack.popMatrix();

            Component currentCentralText = centralText;
            for (int i = 0; i < visibleItems.size(); i++)
            {
                RadialMenuItem item = visibleItems.get(i);
                if (item.isHovered())
                {
                    if (item.getCentralText() != null)
                        currentCentralText = item.getCentralText();
                    break;
                }
            }

            if (currentCentralText != null)
            {
                String text = currentCentralText.getString();
                float textX = (owner.width - font.width(text)) / 2.0f;
                float textY = (owner.height - font.lineHeight) / 2.0f;
                graphics.drawString(font, text, (int)textX, (int)textY, 0xFFFFFFFF, true);
            }

            poseStack.pushMatrix();
            drawTooltips(graphics, mouseX, mouseY);
            poseStack.popMatrix();
        }
    }

    private void updateAnimationState(float partialTicks)
    {
        float openAnimation = 0;
        Screen owner = host.getScreen();
        switch (state)
        {
            case OPENING:
                openAnimation = (float) ((minecraft.level.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);
                if (openAnimation >= 1.0 || getVisibleItemCount() == 0)
                {
                    openAnimation = 1;
                    state = State.NORMAL;
                }
                break;
            case CLOSING:
                openAnimation = 1 - (float) ((minecraft.level.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);
                if (openAnimation <= 0 || getVisibleItemCount() == 0)
                {
                    openAnimation = 0;
                    state = State.CLOSED;
                }
                break;
        }
        animProgress = openAnimation; // MathHelper.clamp(openAnimation, 0, 1);
    }

    private void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY)
    {
        Screen owner = host.getScreen();
        Font fontRenderer = host.getFontRenderer();
        for (int i = 0; i < visibleItems.size(); i++)
        {
            RadialMenuItem item = visibleItems.get(i);
            if (item.isHovered())
            {
                DrawingContext context = new DrawingContext(graphics, owner.width, owner.height, mouseX, mouseY, 0, fontRenderer);
                item.drawTooltips(context);
            }
        }
    }

    private void drawItems(GuiGraphics graphics, int x, int y, int width, int height, Font font)
    {
        iterateVisible((item, s, e) -> {
            float middle = (s + e) * 0.5f;
            float posX = x + itemRadius * (float) Math.cos(middle);
            float posY = y + itemRadius * (float) Math.sin(middle);

            DrawingContext context = new DrawingContext(graphics, width, height, posX, posY, 0, font);
            item.draw(context);
        });
    }

    private void iterateVisible(TriConsumer<RadialMenuItem, Float, Float> consumer)
    {
        int numItems = visibleItems.size();
        for (int i = 0; i < numItems; i++)
        {
            float s = (float) getAngleFor(i - 0.5, numItems);
            float e = (float) getAngleFor(i + 0.5, numItems);

            RadialMenuItem item = visibleItems.get(i);
            consumer.accept(item, s, e);
        }
    }

    private void drawBackground(GuiGraphics graphics, float x, float y, float radiusIn, float radiusOut)
    {
        if (visibleItems.size() > 0)
        {
            iterateVisible((item, s, e) -> {
                int color = item.isHovered() ? backgroundColorHover : backgroundColor;
                drawPieArc(graphics, x, y, radiusIn, radiusOut, s, e, color);
            });
        }
    }

    public void cyclePrevious()
    {
        int numItems = getVisibleItemCount();
        int which = getHovered();
        which--;
        if (which < 0)
            which = numItems - 1;
        setHovered(which);

        moveMouseToItem(which, numItems);
    }

    public void cycleNext()
    {
        int numItems = getVisibleItemCount();
        int which = getHovered();
        if (which < 0)
            which = 0;
        else
        {
            which++;
            if (which >= numItems)
                which = 0;
        }
        moveMouseToItem(which, numItems);
        setHovered(which);
    }

    private void moveMouseToItem(int which, int numItems)
    {
        Screen owner = host.getScreen();
        int x = owner.width / 2;
        int y = owner.height / 2;
        float angle = (float) getAngleFor(which, numItems);
        setMousePosition(
                x + itemRadius * Math.cos(angle),
                y + itemRadius * Math.sin(angle)
        );
    }

    private void setMousePosition(double x, double y)
    {
        Screen owner = host.getScreen();
        Window mainWindow = minecraft.getWindow();
        GLFW.glfwSetCursorPos(mainWindow.handle(), (int) (x * mainWindow.getScreenWidth() / owner.width), (int) (y * mainWindow.getScreenHeight() / owner.height));
    }

    private static final double TWO_PI = 2.0 * Math.PI;

    private void processMouse(int mouseX, int mouseY)
    {
        if (!isReady())
            return;

        int numItems = getVisibleItemCount();

        Screen owner = host.getScreen();
        int x = owner.width / 2;
        int y = owner.height / 2;
        double a = Math.atan2(mouseY - y, mouseX - x);
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
        if (numItems > 0)
        {
            double s0 = getAngleFor(0 - 0.5, numItems);
            double s1 = getAngleFor(numItems - 0.5, numItems);
            while (a < s0)
            {
                a += TWO_PI;
            }
            while (a >= s1)
            {
                a -= TWO_PI;
            }
        }

        int hovered = -1;
        for (int i = 0; i < numItems; i++)
        {
            float s = (float) getAngleFor(i - 0.5, numItems);
            float e = (float) getAngleFor(i + 0.5, numItems);

            if (a >= s && a < e && d >= radiusIn && (d < radiusOut || ConfigData.clipMouseToCircle || ConfigData.allowClickOutsideBounds))
            {
                hovered = i;
                break;
            }
        }
        setHovered(hovered);


        if (ConfigData.clipMouseToCircle)
        {
            Window mainWindow = minecraft.getWindow();

            int windowWidth = mainWindow.getScreenWidth();
            int windowHeight = mainWindow.getScreenHeight();

            double[] xPos = new double[1];
            double[] yPos = new double[1];
            GLFW.glfwGetCursorPos(mainWindow.handle(), xPos, yPos);

            double scaledX = xPos[0] - (windowWidth / 2.0f);
            double scaledY = yPos[0] - (windowHeight / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = radiusOut * (windowWidth / (float) owner.width) * 0.975;

            if (distance > radius)
            {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                GLFW.glfwSetCursorPos(mainWindow.handle(), (int) (windowWidth / 2 + fixedX), (int) (windowHeight / 2 + fixedY));
            }
        }
    }

    private double getAngleFor(double i, int numItems)
    {
        if (numItems == 0)
            return 0;
        double angle = ((i / numItems) + 0.25) * TWO_PI + Math.PI;
        return angle;
    }

    private static final float PRECISION = 2.5f / 360.0f;

    private void drawPieArc(GuiGraphics graphics, float x, float y, float radiusIn, float radiusOut, float startAngle, float endAngle, int color)
    {
        graphics.submitGuiElementRenderState(new BlitPieArc(
                RenderPipelines.GUI, TextureSetup.noTexture(), graphics.pose(), x, y, radiusIn, radiusOut, startAngle, endAngle, color, null
        ));
    }

    public record BlitPieArc(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x,
            float y,
            float radiusIn,
            float radiusOut,
            float startAngle,
            float endAngle,
            int color,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState
    {
        public BlitPieArc(
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                Matrix3x2f pose,
                float x,
                float y,
                float radiusIn,
                float radiusOut,
                float startAngle,
                float endAngle,
                int color,
                @Nullable ScreenRectangle bounds
        ) {
            this(
                    pipeline,
                    textureSetup,
                    pose,
                    x,
                    y,
                    radiusIn,
                    radiusOut,
                    startAngle,
                    endAngle,
                    color,
                    bounds,
                    getBounds(x, y, radiusIn, radiusOut, startAngle, endAngle, pose, bounds)
            );
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            float angle = endAngle - startAngle;
            int sections = Math.max(1, Mth.ceil(angle / PRECISION));

            angle = endAngle - startAngle;

            float slice = angle / sections;

            for (int i = 0; i < sections; i++)
            {
                float angle1 = startAngle + i * slice;
                float angle2 = startAngle + (i + 1) * slice;

                float pos1InX = x + radiusIn * Mth.cos(angle1);
                float pos1InY = y + radiusIn * Mth.sin(angle1);
                float pos1OutX = x + radiusOut * Mth.cos(angle1);
                float pos1OutY = y + radiusOut * Mth.sin(angle1);
                float pos2OutX = x + radiusOut * Mth.cos(angle2);
                float pos2OutY = y + radiusOut * Mth.sin(angle2);
                float pos2InX = x + radiusIn * Mth.cos(angle2);
                float pos2InY = y + radiusIn * Mth.sin(angle2);

                consumer.addVertexWith2DPose(this.pose(), pos1OutX, pos1OutY).setColor(this.color());
                consumer.addVertexWith2DPose(this.pose(), pos1InX, pos1InY).setColor(this.color());
                consumer.addVertexWith2DPose(this.pose(), pos2InX, pos2InY).setColor(this.color());
                consumer.addVertexWith2DPose(this.pose(), pos2OutX, pos2OutY).setColor(this.color());
            }
        }

        private static final float PI_3_HALFS = Mth.PI + Mth.HALF_PI;

        @Nullable
        private static ScreenRectangle getBounds(
                float x, float y, float radiusIn, float radiusOut, float startAngle, float endAngle,
                Matrix3x2f pose,
                @Nullable ScreenRectangle rect
        ) {

            float x0 = x, x1 = x;
            float y0 = y, y1 = y;

            var x2 = x + Mth.cos(startAngle) * radiusIn;
            var y2 = y + Mth.sin(startAngle) * radiusIn;
            x0 = Math.min(x0, x2);
            y0 = Math.min(y0, y2);
            x1 = Math.max(x1, x2);
            y1 = Math.max(y1, y2);

            x2 = x + Mth.cos(endAngle) * radiusIn;
            y2 = y + Mth.sin(endAngle) * radiusIn;
            x0 = Math.min(x0, x2);
            y0 = Math.min(y0, y2);
            x1 = Math.max(x1, x2);
            y1 = Math.max(y1, y2);

            x2 = x + Mth.cos(startAngle) * radiusOut;
            y2 = y + Mth.sin(startAngle) * radiusOut;
            x0 = Math.min(x0, x2);
            y0 = Math.min(y0, y2);
            x1 = Math.max(x1, x2);
            y1 = Math.max(y1, y2);

            x2 = x + Mth.cos(endAngle) * radiusOut;
            y2 = y + Mth.sin(endAngle) * radiusOut;
            x0 = Math.min(x0, x2);
            y0 = Math.min(y0, y2);
            x1 = Math.max(x1, x2);
            y1 = Math.max(y1, y2);

            if (0 > startAngle && 0 < endAngle)
            {
                x2 = x + Mth.cos(0) * radiusOut;
                y2 = y + Mth.sin(0) * radiusOut;
                x0 = Math.min(x0, x2);
                y0 = Math.min(y0, y2);
                x1 = Math.max(x1, x2);
                y1 = Math.max(y1, y2);
            }

            if (Mth.HALF_PI > startAngle && Mth.HALF_PI < endAngle)
            {
                x2 = x + Mth.cos(Mth.HALF_PI) * radiusOut;
                y2 = y + Mth.sin(Mth.HALF_PI) * radiusOut;
                x0 = Math.min(x0, x2);
                y0 = Math.min(y0, y2);
                x1 = Math.max(x1, x2);
                y1 = Math.max(y1, y2);
            }

            if (Mth.PI > startAngle && Mth.PI < endAngle)
            {
                x2 = x + Mth.cos(Mth.PI) * radiusOut;
                y2 = y + Mth.sin(Mth.PI) * radiusOut;
                x0 = Math.min(x0, x2);
                y0 = Math.min(y0, y2);
                x1 = Math.max(x1, x2);
                y1 = Math.max(y1, y2);
            }

            if (PI_3_HALFS > startAngle && PI_3_HALFS < endAngle)
            {
                x2 = x + Mth.cos(Mth.PI) * radiusOut;
                y2 = y + Mth.sin(Mth.PI) * radiusOut;
                x0 = Math.min(x0, x2);
                y0 = Math.min(y0, y2);
                x1 = Math.max(x1, x2);
                y1 = Math.max(y1, y2);
            }

            ScreenRectangle screenrectangle = new ScreenRectangle(Mth.floor(x0), Mth.floor(y0), Mth.ceil(x1 - x0), Mth.ceil(y1 - y0)).transformMaxBounds(pose);
            return rect != null ? rect.intersection(screenrectangle) : screenrectangle;
        }
    }

}
