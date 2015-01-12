package com.maxpowa.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.maxpowa.WikiTool;
import com.maxpowa.WikiUtil;
import com.maxpowa.threading.RunnableFetchPageImages.FetchResult;

public class ImageTemplate extends IChatStyleExtra {

    public static final ResourceLocation BUTTONS = new ResourceLocation(WikiTool.MOD_ID+":textures/gui/buttons.png");
    
    private String image_name;
    private String page;
    
    private FetchResult imageData = null;
    
    public ImageTemplate(String page, String image_name) {
        this.page = page;
        this.image_name = image_name;
    }
    
    public void draw(String text, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (imageData == null) {
            imageData = WikiUtil.getImage(page, image_name);
        } else if (imageData.needsReload()) {
            imageData.reloadDynamicTexture();
        } else {
            Minecraft mc = Minecraft.getMinecraft();
            GL11.glPushMatrix();
            mc.renderEngine.bindTexture(imageData.getResourceLocation());
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, imageData.getWidth(), imageData.getHeight(), imageData.getWidth(), imageData.getHeight());
            GL11.glPopMatrix();
        }
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
