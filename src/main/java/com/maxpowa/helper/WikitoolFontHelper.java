package com.maxpowa.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class WikitoolFontHelper {

    @SuppressWarnings("unchecked")
    public static List<IChatComponent> listIChatComponentToWidth(IChatComponent input, int width) {
        List<IChatComponent> output = new ArrayList<IChatComponent>();
        Iterator<IChatComponent> inputIterator = input.iterator();

        int currentLineWidth = 0;
        IChatComponent lastComponent = new ChatComponentText("");

        // Skip the first object, because the first object is the entire thing.
        inputIterator.next();

        while (inputIterator.hasNext()) {
            IChatComponent current = inputIterator.next();
            String raw = current.getUnformattedText();
            int sectionWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(raw);
            currentLineWidth += sectionWidth;

            if (currentLineWidth > width) {
                lastComponent = chopToWidth(current, lastComponent, output, width, currentLineWidth);
                currentLineWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(lastComponent.getUnformattedText());
            } else if (currentLineWidth == width) {
                output.add(lastComponent.createCopy());
                lastComponent = new ChatComponentText("");
                currentLineWidth = 0;
            } else if (raw.equalsIgnoreCase("\n")) {
                output.add(lastComponent.createCopy());
                lastComponent = new ChatComponentText("");
                currentLineWidth = 0;
            } else {
                lastComponent.appendSibling(current.createCopy());
            }
        }
        return output;
    }

    private static IChatComponent chopToWidth(IChatComponent current, IChatComponent lastComponent, List<IChatComponent> output, int maxWidth, int currentLineWidth) {
        int secWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(current.getUnformattedText());
        if (secWidth+currentLineWidth < maxWidth) {
            lastComponent.appendSibling(current);
            return lastComponent;
        }
        
        int splitpoint = sizeStringToWidth(current.getUnformattedText(), Minecraft.getMinecraft().fontRenderer.getStringWidth(current.getUnformattedText()) - Math.abs(maxWidth - currentLineWidth));
        String thisLine = current.getUnformattedText().substring(0, splitpoint);
        char splitChar = current.getUnformattedText().charAt(splitpoint);
        boolean flag = splitChar == 32 || splitChar == 10;

        ChatComponentText line = new ChatComponentText(thisLine);
        line.setChatStyle(current.getChatStyle());
        
        lastComponent.appendSibling(line);
        output.add(lastComponent.createCopy());
        
        // if this is longer than the width then we will have issues.
        IChatComponent next = new ChatComponentText(current.getUnformattedText().substring(splitpoint + (flag ? 1 : 0)));
        next.setChatStyle(current.getChatStyle());

//        if (Minecraft.getMinecraft().fontRenderer.getStringWidth(next.getUnformattedText())-1 > maxWidth) {
//            next = chopToWidth(next.createCopy(), new ChatComponentText(""), output, maxWidth, 0);
//        }
        
        lastComponent = new ChatComponentText("");
        lastComponent.appendSibling(next);
        return lastComponent;
    }

    private static int sizeStringToWidth(String p_78259_1_, int p_78259_2_) {
        int j = p_78259_1_.length();
        int k = 0;
        int l = 0;
        int i1 = -1;

        for (boolean flag = false; l < j; ++l) {
            char c0 = p_78259_1_.charAt(l);

            switch (c0) {
            case 10:
                --l;
                break;
            case 167:
                if (l < j - 1) {
                    ++l;
                    char c1 = p_78259_1_.charAt(l);

                    if (c1 != 108 && c1 != 76) {
                        if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
                            flag = false;
                        }
                    } else {
                        flag = true;
                    }
                }

                break;
            case 32:
                i1 = l;
            default:
                k += Minecraft.getMinecraft().fontRenderer.getCharWidth(c0);

                if (flag) {
                    ++k;
                }
            }

            if (c0 == 10) {
                ++l;
                i1 = l;
                break;
            }

            if (k > p_78259_2_) {
                break;
            }
        }

        return l != j && i1 != -1 && i1 < l ? i1 : l;
    }

    /**
     * Checks if the char code is a hexadecimal character, used to set colour.
     */
    private static boolean isFormatColor(char p_78272_0_) {
        return p_78272_0_ >= 48 && p_78272_0_ <= 57 || p_78272_0_ >= 97 && p_78272_0_ <= 102 || p_78272_0_ >= 65 && p_78272_0_ <= 70;
    }

}