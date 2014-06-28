package com.maxpowa.ui.wiki;

import java.util.List;

import org.lwjgl.input.Mouse;

import de.fau.cs.osr.ptk.common.ast.AstNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiWikiPage extends GuiScreen {
    
    GuiScreen parentScreen;
    AstNode content;
    int alpha = 1;
    int topLine = 0;
    List<String> strings;
    
    @SuppressWarnings("unchecked")
    public GuiWikiPage(GuiScreen parentScreen, AstNode astNode, String subCategory) {
        mc = Minecraft.getMinecraft();
        this.parentScreen = parentScreen;
        this.content = astNode;
        strings = mc.fontRenderer.listFormattedStringToWidth(content.getPropertyCount()+"", mc.currentScreen.width - 30);
        if (subCategory != "") {
            for (String s : strings) {
                if (s.contains("== "+subCategory+" ==") || s.contains("=== "+subCategory+" ===") || s.contains("==== "+subCategory+" ====") ){
                    break;
                }
                topLine++;
            }
        }
    }
    
    public void mouseWheel() {
        int maxLines = mc.currentScreen.height/10 - 3;
        int deltaWheel = Mouse.getDWheel();
        if (deltaWheel > 0 && topLine > 0) {
            topLine--;
        } else if (deltaWheel < 0 && topLine < strings.size() - maxLines) {
            topLine++;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int maxLines = mc.currentScreen.height/10 - 3;
        int yOffset = 0;
        if (alpha < 255)
            alpha+=15;
        
        int lineNum = 0;
        for (String s : strings) {
            lineNum++;
            if (lineNum > topLine && lineNum < topLine+maxLines)
                mc.fontRenderer.drawString(s, 15, (yOffset += 10)+20, argb(alpha, 255, 255, 255));
        }
    }
    
    public static int argb(int A, int R, int G, int B){     
        byte[] colorByteArr = { (byte) A, (byte) R, (byte) G, (byte) B };
        return byteArrToInt(colorByteArr);
    }
    
    public static final int byteArrToInt(byte[] colorByteArr) {
        return (colorByteArr[0] << 24) + ((colorByteArr[1] & 0xFF) << 16) + ((colorByteArr[2] & 0xFF) << 8) + (colorByteArr[3] & 0xFF);
    }
    
}
