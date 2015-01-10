package com.maxpowa.ui;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.maxpowa.WikiUtil;

public class GuiWikiLoading extends GuiButton {

    Random random = new Random();
    int shift = 29;
    int rotate = 0;
    long lastAnimationMove = 0L;

    public GuiWikiLoading(int par2, int par3) {
        super(0, par2, par3, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.renderEngine.bindTexture(new ResourceLocation("wikitool",
                "textures/gui/book_sprite.png"));
        mc.currentScreen.drawTexturedModalRect(xPosition, yPosition, shift, 25,
                29, 23);

        if (System.nanoTime() - lastAnimationMove > 200000000L) {
            shift += 29;
            lastAnimationMove = System.nanoTime();
        }
        if (shift >= 87) {
            shift = 0;
        } else if (shift < 0) {
            shift = 58;
        }

        // For spinny, ring type animation.
        // if (System.nanoTime() - lastAnimationMove > 64000000L) {
        // rotate+=20;
        // lastAnimationMove = System.nanoTime();
        // }
        // GL11.glScalef(0.25F, 0.25F, 1.0F);
        // GL11.glTranslatef(xPosition*4, yPosition*4, 0F);
        // GL11.glRotatef(rotate, 0F, 0F, 1F);
        // mc.renderEngine.bindTexture(new
        // ResourceLocation("wikitool","textures/gui/ring.png"));
        // mc.currentScreen.drawTexturedModalRect(-90, -90, 0, 0, 180, 180);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        switch (WikiUtil.state) {
        case SEARCHING:
            this.drawCenteredString(mc.fontRenderer, "Searching...",
                    xPosition + 17, yPosition + 29, 0xFFFFFF);
            break;
        case READING:
            this.drawCenteredString(mc.fontRenderer, "Reading...",
                    xPosition + 17, yPosition + 29, 0xFFFFFF);
            break;
        case PARSING:
            this.drawCenteredString(mc.fontRenderer, "Parsing...",
                    xPosition + 17, yPosition + 29, 0xFFFFFF);
            break;
        case DONE:
            this.drawCenteredString(mc.fontRenderer,
                    "Error! No information found.", xPosition + 17,
                    yPosition + 29, 0xFFFFFF);
            break;
        case NONE:
            this.drawCenteredString(mc.fontRenderer,
                    "Error! No information found.", xPosition + 17,
                    yPosition + 29, 0xFFFFFF);
            break;
        }
    }

}
