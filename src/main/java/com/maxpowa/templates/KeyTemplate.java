package com.maxpowa.templates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.maxpowa.WikiTool;

public class KeyTemplate extends ChatStyle {

    public static final ResourceLocation BUTTONS = new ResourceLocation(WikiTool.MOD_ID+":textures/gui/buttons.png");
    
    private String key_char = "**";
    
    public KeyTemplate(String key_char) {
        this.key_char = key_char;
    }
    
    public void drawKey(int x, int y, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glPushMatrix();
        mc.renderEngine.bindTexture(BUTTONS);
        GL11.glScalef(0.5f, 0.5f, 1.0f);
        drawModalRectWithCustomSizedTexture(x*2, y*2, 0, 0, 16, 16, 256, 256);
        mc.fontRenderer.drawString(key_char, x*2, y*2, 0xFFFFFF);
        GL11.glPopMatrix();
    }
    
    public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
        float f4 = 1.0F / textureWidth;
        float f5 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)x, (double)(y + height), 0.0D, (double)(u * f4), (double)((v + (float)height) * f5));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0.0D, (double)((u + (float)width) * f4), (double)((v + (float)height) * f5));
        tessellator.addVertexWithUV((double)(x + width), (double)y, 0.0D, (double)((u + (float)width) * f4), (double)(v * f5));
        tessellator.addVertexWithUV((double)x, (double)y, 0.0D, (double)(u * f4), (double)(v * f5));
        tessellator.draw();
    }

}
