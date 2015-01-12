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
        Minecraft mc = Minecraft.getMinecraft();
        List<IChatComponent> output = new ArrayList<IChatComponent>();
        Iterator<IChatComponent> inputIterator = input.iterator();

        int currentLineWidth = 0;
        IChatComponent lastComponent = new ChatComponentText("");

        // Skip the first object, because the first object is the entire thing.
        inputIterator.next();

        while (inputIterator.hasNext()) {
            IChatComponent current = inputIterator.next();
            
            // Split on spaces, but retain the spaces.
            String[] tokens = current.getUnformattedText().split("((?<=[\n ])|(?=[\n ]))");
            for (int i = 0; i < tokens.length; i++) {
                String word = tokens[i];
                
                if (word.equalsIgnoreCase("\n")) {
                    // prevents duplicate newlines
                    if (output.size() != 0 && !(lastComponent.getUnformattedText().trim().equalsIgnoreCase("") && output.get(output.size()-1).getUnformattedText().trim().equalsIgnoreCase("")))
                        output.add(lastComponent.createCopy());
                    lastComponent = new ChatComponentText("");
                    currentLineWidth = 0;
                    continue;
                }
                
                if (currentLineWidth + mc.fontRenderer.getStringWidth(word) > width) {
                    output.add(lastComponent.createCopy());
                    lastComponent = new ChatComponentText("");
                    currentLineWidth = 0;
                }
                
                ChatComponentText wordcmp = new ChatComponentText(word);
                wordcmp.setChatStyle(current.getChatStyle());
                lastComponent.appendSibling(wordcmp);
                currentLineWidth = mc.fontRenderer.getStringWidth(lastComponent.getUnformattedText());
            }
        }
        return output;
    }

}
