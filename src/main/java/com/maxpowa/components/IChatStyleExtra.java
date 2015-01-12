package com.maxpowa.components;

import net.minecraft.util.ChatStyle;

public abstract class IChatStyleExtra extends ChatStyle {

    /**
     * Intended for drawing any extra stuff that may need to be rendered.
     * @param text
     * @param x
     * @param y
     * @param width
     * @param height
     * @param mouseX
     * @param mouseY
     */
    public abstract void draw(String text, int x, int y, int width, int height, int mouseX, int mouseY);
    
    @Override
    public IChatStyleExtra createDeepCopy()
    {
        IChatStyleExtra chatstyle = new BasicStyle();
        chatstyle.setBold(Boolean.valueOf(this.getBold()));
        chatstyle.setItalic(Boolean.valueOf(this.getItalic()));
        chatstyle.setStrikethrough(Boolean.valueOf(this.getStrikethrough()));
        chatstyle.setUnderlined(Boolean.valueOf(this.getUnderlined()));
        chatstyle.setObfuscated(Boolean.valueOf(this.getObfuscated()));
        chatstyle.setColor(this.getColor());
        chatstyle.setChatClickEvent(this.getChatClickEvent());
        chatstyle.setChatHoverEvent(this.getChatHoverEvent());
        return chatstyle;
    }
    
    public static class BasicStyle extends IChatStyleExtra {

        @Override
        public void draw(String text, int x, int y, int width, int height, int mouseX, int mouseY) {
            //NO-OP
        }
        
    }
}