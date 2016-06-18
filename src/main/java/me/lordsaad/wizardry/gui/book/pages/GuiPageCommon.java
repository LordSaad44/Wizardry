package me.lordsaad.wizardry.gui.book.pages;

import me.lordsaad.wizardry.Wizardry;
import me.lordsaad.wizardry.api.Constants;
import me.lordsaad.wizardry.gui.book.Tippable;
import me.lordsaad.wizardry.gui.book.util.DataNode;
import me.lordsaad.wizardry.gui.book.util.PageRegistry;
import me.lordsaad.wizardry.gui.book.util.PathUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public abstract class GuiPageCommon extends Tippable {

    protected GuiScreen parent;
    protected String path;
    protected int page;
    protected int viewWidth, viewHeight;
    protected int screenScale;

    { /* helpers */ }

    public GuiPageCommon(GuiScreen parent, DataNode data, DataNode globalData, String path, int page) {
        this.parent = parent;
        this.path = path;
        this.page = page;
        this.viewWidth = 115;
        this.viewHeight = 154;
        if (globalData.get("title").isString()) {
            this.title = globalData.get("title").asString();
        }
        setHasNavReturn(true);
        setHasNavNext(data.get("hasNext").exists());
        setHasNavPrev(data.get("hasPrev").exists());
    }

    public abstract void drawPage(int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        mc.fontRendererObj.setUnicodeFlag(true);

        GlStateManager.pushMatrix();
        int viewLeft = left + 15, viewTop = top + 12;
        GlStateManager.translate(viewLeft, viewTop, 0);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(viewLeft * screenScale, mc.displayHeight - (viewTop + viewHeight) * screenScale,
                viewWidth * screenScale, viewHeight * screenScale);
        drawPage(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        mc.fontRendererObj.setUnicodeFlag(false);
    }

    @Override
    public void initGui() {
        super.initGui();
        screenScale = new ScaledResolution(mc).getScaleFactor();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == Constants.GuiButtons.NAV_BAR_INDEX) {
            mc.displayGuiScreen(parent);
        }
        if (button.id == Constants.GuiButtons.NAV_BAR_NEXT) {
            openPage(path, page + 1);
        }
        if (button.id == Constants.GuiButtons.NAV_BAR_BACK) {
            openPage(path, page - 1);
        }
    }

    public void openPage(String path, int page) {
        mc.displayGuiScreen(PageRegistry.construct(this.path.equals(path) ? this.parent : this, path, page));
    }

    public ResourceLocation pageResource(String path) {
        return new ResourceLocation(Wizardry.MODID, PathUtils.resolve("textures/" + PathUtils.resolve(PathUtils.parent(this.path), path)));
    }
}