package me.lordsaad.wizardry.book;

import me.lordsaad.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Saad on 4/13/2016.
 */
public class MainIndex extends Tippable {

    private boolean didInit = false;
    private HashMap<GuiButton, String> tips = new HashMap<>();
    private HashMap<GuiButton, ResourceLocation> icons = new HashMap<>();
    private int iconSize = 25, iconSeparation = 15;

    @Override
    public void initGui() {
        super.initGui();
        enableNavBar(false);
    }

    /**
     * Initialize all the icons on the front page
     * with the tips from icon-tips.txt
     */
    private void initIndexButtons() {
        int ID = 0, row = 0, column = 0;

        List<String> tips = new ArrayList<>();
        try {
            String theString = IOUtils.toString(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(Wizardry.MODID, "textures/book/icons/icon-tips.txt")).getInputStream(), "UTF-8");
            for (String tip : theString.split("\n")) {
                if (tip != null)
                    tips.add(tip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String tip : tips) {
            ResourceLocation location = new ResourceLocation(Wizardry.MODID, "textures/book/icons/" + tip.split("=")[0] + ".png");

            int x = left + iconSeparation + (row * iconSize) + (row * iconSeparation);
            int y = top + iconSeparation + (column * iconSize) + (column * iconSeparation);
            addNewIndexButton(new Button(ID++, x, y, iconSize, iconSize), location, tip.split("=")[1]);
            if (row >= 2) {
                row = 0;
                column++;
            } else row++;
        }

        didInit = true;
    }

    private void addNewIndexButton(Button button, ResourceLocation regularTexture, String tip) {
        buttonList.add(button);
        tips.put(button, tip);
        icons.put(button, regularTexture);
    }

    /**
     * When the player clicks a button.
     *
     * @param button the button that was clicked.
     * @throws IOException
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.thePlayer.openGui(Wizardry.instance, GuiHandler.BASICS, mc.theWorld, (int) mc.thePlayer.posX, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ);
            clearTips();
        }
    }

    /**
     * Render everything in the index
     *
     * @param mouseX       The current position of the mouse on the x axis.
     * @param mouseY       The current position of the mouse on the y axis.
     * @param partialTicks Useless thing.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Initialize if we didn't already.
        if (!didInit) initIndexButtons();

        int row = 0, column = 0;
        for (GuiButton button : buttonList) {

            boolean inside = mouseX >= button.xPosition && mouseX < button.xPosition + button.width && mouseY >= button.yPosition && mouseY < button.yPosition + button.height;
            int x = left + iconSeparation + (row * iconSize) + (row * iconSeparation);
            int y = top + iconSeparation + (column * iconSize) + (column * iconSeparation);
            if (row >= 2) {
                row = 0;
                column++;
            } else row++;

            button.xPosition = x;
            button.yPosition = y;
            button.width = iconSize;
            button.height = iconSize;
            mc.renderEngine.bindTexture(icons.get(button));

            if (inside) {
                Tip tip = setTip(tips.get(button));
                if (tip != null) tipManager.put(button, tip.getID());
                GlStateManager.color(0F, 191F, 255F, 1F);
            } else {
                if (tipManager.containsKey(button)) {
                    removeTip(tipManager.get(button));
                    tipManager.remove(button);
                }
                GlStateManager.color(0F, 0F, 0F, 1F);
            }

            drawScaledCustomSizeModalRect(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize, iconSize, iconSize);
            GlStateManager.color(1F, 1F, 1F, 1F);
        }

        mc.renderEngine.bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect((width / 2) - 66, (float) (top - 20), 19, 182, 133, 14);
        fontRendererObj.setUnicodeFlag(false);
        fontRendererObj.setBidiFlag(false);
        fontRendererObj.drawString("Physics Book", (width / 2) - 30, (float) (top - 20) + 4, 0, false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
