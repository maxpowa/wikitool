package com.maxpowa.ui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.maxpowa.WikiTool;
import com.maxpowa.WikiUtil;
import com.maxpowa.WikiUtil.FetchState;
import com.maxpowa.ui.wiki.GuiWikiPage;

public class GuiWikiScreen extends GuiScreen {

    private GuiScreen parentScreen;
    private long lastAnimationMove = 0L;
    private int index = 0;
    private String title;
    private String shortTitle;
    private String showingTitle = "";
    private String subCategory = "";
    private GuiWikiLoading load;
    private GuiWikiPage wikiPage;
    private String pageTitle;

    public GuiWikiScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        if (WikiTool.getEquiv(parentScreen).contains("#")) {
            shortTitle = WikiTool.getEquiv(parentScreen).replaceAll("_", " ").split("#")[1];
            subCategory = shortTitle;
        } else {
            shortTitle = WikiTool.getEquiv(parentScreen).replaceAll("_", " ");
        }
        title = "WikiTool - " + shortTitle;
        this.pageTitle = WikiTool.getEquiv(parentScreen);
        WikiUtil.getPage(pageTitle, "minecraft.gamepedia.com");
    }

    public GuiWikiScreen(String title, GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.shortTitle = title;
        this.title = "WikiTool - " + title;
        this.pageTitle = title;
        WikiUtil.getPage(pageTitle, "minecraft.gamepedia.com");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (wikiPage != null)
            wikiPage.mouseWheel();
        // parentScreen.drawScreen(mouseX, mouseY, partialTicks);
        this.startGL();
        this.drawGradientRect(0, 0, width, height, -1072689136, -804253680);
        this.endGL();

        this.startGL();
        mc.renderEngine.bindTexture(new ResourceLocation("wikitool", "textures/gui/book_sprite.png"));
        mc.currentScreen.drawTexturedModalRect(5, 2, 174, 0, 29, 25);

        if (System.nanoTime() - lastAnimationMove > 650000L && index < title.length()) {
            showingTitle += title.toCharArray()[index];
            index++;
            lastAnimationMove = System.nanoTime();
        }
        mc.fontRenderer.drawString(showingTitle, 35, 12, 0xFFFFFF);
        this.endGL();

        this.startGL();
        if (WikiUtil.state != FetchState.DONE || WikiUtil.getPage(this.pageTitle, "minecraft.gamepedia.com") == null) {
            load.drawButton(mc, mouseX, mouseY);
        } else {
            if (wikiPage != null) {
                wikiPage.height = this.height;
                wikiPage.width = this.width;
                wikiPage.drawScreen(mouseX, mouseY, 0);
            } else {
                wikiPage = new GuiWikiPage(this, WikiUtil.getPage(this.pageTitle, "minecraft.gamepedia.com"), subCategory);
            }
        }
        this.endGL();
    }

    private void startGL() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private void endGL() {
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void updateScreen() {
        parentScreen.updateScreen();
        super.updateScreen();
    }

    /**
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2) {
        if (par2 == Keyboard.KEY_ESCAPE)
            if (parentScreen instanceof GuiGameOver) {
                mc.displayGuiScreen(new GuiGameOver());
            } else {
                mc.displayGuiScreen(parentScreen);
            }
        else {
            super.keyTyped(par1, par2);
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void actionPerformed(GuiButton p_146284_1_) {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        load = new GuiWikiLoading(width / 2 - 14, height / 2 - 12);
        wikiPage = null;
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in
     * single-player
     */
    @Override
    public boolean doesGuiPauseGame() {
        return parentScreen != null ? parentScreen.doesGuiPauseGame() : false;
    }
}
