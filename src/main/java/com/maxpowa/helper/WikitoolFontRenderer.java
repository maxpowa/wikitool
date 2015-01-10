package com.maxpowa.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

public class WikitoolFontRenderer extends FontRenderer {

    public static WikitoolFontRenderer INSTANCE;

    static {
        INSTANCE = new WikitoolFontRenderer();
    }

    private WikitoolFontRenderer() {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
    }

    public static WikitoolFontRenderer getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public List<IChatComponent> listIChatComponentToWidth(IChatComponent input, int width) {
        List<IChatComponent> output = new ArrayList<IChatComponent>();
        Iterator<IChatComponent> inputIterator = input.iterator();

        int currentLineWidth = 0;
        IChatComponent lastComponent = new ChatComponentText("");

        // Skip the first object, because the first object is the entire thing.
        inputIterator.next();

        while (inputIterator.hasNext()) {
            IChatComponent current = inputIterator.next();
            String raw = current.getUnformattedText();
            int sectionWidth = this.getStringWidth(raw);
            currentLineWidth += sectionWidth;

            if (currentLineWidth > width) {
                int splitpoint = this.sizeStringToWidth(raw, sectionWidth - (currentLineWidth - width));
                String thisLine = current.getUnformattedText().substring(0, splitpoint);
                char splitChar = current.getUnformattedText().charAt(splitpoint);
                boolean flag = splitChar == 32 || splitChar == 10;

                ChatComponentText line = new ChatComponentText(thisLine);
                line.setChatStyle(current.getChatStyle());
                // if this is longer than the width then we will have issues.
                ChatComponentText next = new ChatComponentText(current.getUnformattedText().substring(splitpoint + (flag ? 1 : 0)));
                next.setChatStyle(current.getChatStyle());

                lastComponent.appendSibling(line);
                output.add(lastComponent.createCopy());

                lastComponent = new ChatComponentText("");
                lastComponent.appendSibling(next);
                currentLineWidth = this.getStringWidth(lastComponent.getUnformattedText());
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

    private int sizeStringToWidth(String p_78259_1_, int p_78259_2_) {
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
                k += this.getCharWidth(c0);

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
