package github.kasuminova.novaeng.client.gui.widget.estorage;

import github.kasuminova.mmce.client.gui.util.AnimationValue;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Column;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.novaeng.NovaEngineeringCore;
import github.kasuminova.novaeng.client.gui.widget.estorage.event.ESGraphFocusUpdateEvent;
import github.kasuminova.novaeng.common.util.RandomUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;

public abstract class Graph extends Column {
    public static final ResourceLocation BG_TEX_RES = new ResourceLocation(NovaEngineeringCore.MOD_ID, "textures/gui/estorage_controller_elements.png");

    protected final EStorageGraph graphParent;
    protected final MultiLineLabel label;
    protected final AnimationValue value = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);

    protected final int bgTexX;
    protected final int bgTexY;
    protected final int bgTexWidth;
    protected final int bgTexHeight;
    
    protected final boolean reverseColor;

    protected boolean focused = false;

    public Graph(final EStorageGraph graphParent,
                 final int absX,
                 final int absY,
                 final int width,
                 final int height,
                 final int randomOffset,
                 final int bgTexX,
                 final int bgTexY,
                 final int bgTexWidth,
                 final int bgTexHeight,
                 final boolean leftAlign,
                 final boolean reverseColor) {
        this.graphParent = graphParent;
        this.label = new MultiLineLabel(Collections.emptyList());
        this.bgTexX = bgTexX;
        this.bgTexY = bgTexY;
        this.bgTexWidth = bgTexWidth;
        this.bgTexHeight = bgTexHeight;
        this.width = width;
        this.height = height;
        this.reverseColor = reverseColor;
        this.label.setAutoRecalculateSize(false)
                .setScale(0.6F)
                .setVerticalCentering(true)
                .setWidth((int) (width * 0.7))
                .setHeight(9)
                .setMargin(2, 2, 0, 0);
        setRightAligned(!leftAlign);
        addWidget(this.label);
        setUseScissor(false);
        setAbsXY(
                absX + (RandomUtils.nextInt(randomOffset * 2) - randomOffset),
                absY + (RandomUtils.nextInt(randomOffset * 2) - randomOffset)
        );
    }

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (isMouseOverGraphText(mousePos.mouseX(), mousePos.mouseY()) && !focused) {
            focused = true;
            graphParent.getControllerGUI().onGraphFocusUpdate(this);
            graphParent.getGraphBar().setPercentage(value, reverseColor);
        }
        super.preRenderInternal(gui, renderSize, renderPos, mousePos);
    }

    public boolean isMouseOverGraphText(int mouseX, int mouseY) {
        if (isInvisible()) {
            return false;
        }

        int endX = width;
        int endY = height - 6;
        return mouseX >= 0 && mouseX <= endX && mouseY >= 0 && mouseY <= endY;
    }

    @Override
    protected void renderInternal(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!focused) {
            GlStateManager.color(1F, 1F, 1F, .4F);
        }
        GuiScreen gui = widgetGui.getGui();
        gui.mc.getTextureManager().bindTexture(BG_TEX_RES);
        gui.drawTexturedModalRect(
                renderPos.posX(), renderPos.posY() + this.label.getHeight(),
                bgTexX, bgTexY,
                bgTexWidth, bgTexHeight
        );
        GlStateManager.color(1F, 1F, 1F, 1F);
        super.renderInternal(widgetGui, renderSize, renderPos, mousePos);
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof ESGraphFocusUpdateEvent focusUpdateEvent && focusUpdateEvent.getGraph() != this && focused) {
            focused = false;
        }
        return super.onGuiEvent(event);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
