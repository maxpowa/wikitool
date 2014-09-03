package com.maxpowa.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.maxpowa.WikiTool;


public class GuiWikiButton extends GuiButton {

    private long pageFlipTime = System.nanoTime();
    private int spriteIndex = 0;
    private boolean hovering = false;

    public GuiWikiButton(int posX, int posY) {
        super(0, posX, posY, 29, 25, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        //super.drawButton(mc, mouseX, mouseY);
        hovering = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
        
        if (System.nanoTime() - pageFlipTime > 65000000L && hovering) {
            if (spriteIndex < 174) {
                spriteIndex += 29;
            }
            pageFlipTime = System.nanoTime();
        } else if (System.nanoTime() - pageFlipTime > 65000000L && !hovering) {
            if (spriteIndex > 0) {
                spriteIndex -= 29;
            }
            pageFlipTime = System.nanoTime();
        }
        
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(new ResourceLocation("wikitool", "textures/gui/book_sprite.png"));
        mc.currentScreen.drawTexturedModalRect(xPosition, yPosition, spriteIndex, 0, 29, 25);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        
        if (Mouse.isButtonDown(0) && hovering && !(mc.currentScreen instanceof GuiWikiScreen)) {
            if (mc.currentScreen instanceof GuiDispenser) {
                GuiDispenser dispenser = (GuiDispenser) mc.currentScreen;
                if (dispenser.tileDispenser instanceof TileEntityDropper)
                    mc.displayGuiScreen(new GuiWikiScreen("Dropper", mc.currentScreen));
                else
                    mc.displayGuiScreen(new GuiWikiScreen(mc.currentScreen));
            } else {
                WikiTool.log.info("Current Screen: " + mc.currentScreen.getClass().toString());
                mc.displayGuiScreen(new GuiWikiScreen(mc.currentScreen));
            }
        }
    }
}
