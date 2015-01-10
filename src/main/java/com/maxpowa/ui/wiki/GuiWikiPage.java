package com.maxpowa.ui.wiki;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import scala.actors.threadpool.Arrays;

import com.google.common.collect.Sets;
import com.maxpowa.WikiTool;
import com.maxpowa.helper.ChatComponentPrinter;
import com.maxpowa.helper.WikitoolFontRenderer;
import com.maxpowa.ui.GuiWikiScreen;

import de.fau.cs.osr.ptk.common.ast.AstNode;

public class GuiWikiPage extends GuiScreen {

    GuiScreen parentScreen;
    int alpha = 1;
    int topLine = 0;
    List<IChatComponent> strings;

    // IChatComponent out;

    public GuiWikiPage(GuiScreen parentScreen, AstNode astNode,
            String subCategory) {
        mc = Minecraft.getMinecraft();
        this.parentScreen = parentScreen;

        IChatComponent cmp = ChatComponentPrinter.print(astNode, subCategory);

        // out = cmp;
        // strings =
        // WikitoolFontRenderer.getInstance().listIChatComponentToWidth(cmp,
        // mc.currentScreen.width - 30);
        strings = WikitoolFontRenderer.getInstance().listIChatComponentToWidth(
                cmp, 50);
    }

    public void mouseWheel() {
        int maxLines = mc.currentScreen.height / 10 - 3;
        int deltaWheel = Mouse.getDWheel();
        if (deltaWheel > 0 && topLine > 0) {
            topLine--;
        } else if (deltaWheel < 0 && topLine < strings.size() - maxLines) {
            topLine++;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int maxLines = mc.currentScreen.height / 10 - 3;
        int yOffset = 0;
        if (alpha < 255)
            alpha += 15;

        int lineNum = 0;
        for (IChatComponent cmp : strings) {
            lineNum++;
            if (lineNum > topLine && lineNum < topLine + maxLines)
                this.drawString(cmp, 15, (yOffset += 10) + 20, mouseX, mouseY,
                        argb(alpha, 255, 255, 255), false);
        }
    }

    @SuppressWarnings("unchecked")
    public void drawString(IChatComponent cmp, int x, int y, int mouseX,
            int mouseY, int color, boolean shadow) {
        mc.fontRenderer.drawString(cmp.getFormattedText(), x, y, color, shadow);

        for (IChatComponent child : (Iterable<IChatComponent>) cmp) {
            ChatStyle style = child.getChatStyle();
            int linkWidth = mc.fontRenderer.getStringWidth(child
                    .getFormattedText());
            int linkX = x
                    + mc.fontRenderer.getStringWidth(cmp.getFormattedText()
                            .substring(
                                    0,
                                    cmp.getFormattedText().indexOf(
                                            child.getFormattedText())));
            if (linkX <= mouseX && mouseX <= linkX + linkWidth && y <= mouseY
                    && mouseY < y + 10) {
                if (style.getChatHoverEvent() != null
                        && !style.getChatHoverEvent().getValue()
                                .getFormattedText().isEmpty()) {
                    // WikiTool.log.info("Found a hover event on text: \""+child.getUnformattedText()+"\"");
                    this.drawLinkHighlight(
                            Arrays.asList(style.getChatHoverEvent().getValue()
                                    .getFormattedText().split("\n")), mouseX,
                            mouseY, Minecraft.getMinecraft().fontRenderer);
                    if (Mouse.isButtonDown(0)) {
                        switch (style.getChatClickEvent().getAction()) {
                        case OPEN_FILE:
                            break;
                        case OPEN_URL:
                            try {
                                URI uri = new URI(style.getChatClickEvent()
                                        .getValue());

                                if (!Sets
                                        .newHashSet(
                                                new String[] { "http", "https" })
                                        .contains(uri.getScheme().toLowerCase())) {
                                    throw new URISyntaxException(style
                                            .getChatClickEvent().getValue(),
                                            "Unsupported protocol: "
                                                    + uri.getScheme()
                                                            .toLowerCase());
                                }

                                this.openUri(uri);
                            } catch (URISyntaxException urisyntaxexception) {
                                WikiTool.log.error("Can\'t open url for "
                                        + style.getChatClickEvent(),
                                        urisyntaxexception);
                            }
                            break;
                        case RUN_COMMAND:
                            mc.displayGuiScreen(new GuiWikiScreen(style
                                    .getChatClickEvent().getValue(),
                                    mc.currentScreen));
                            break;
                        case SUGGEST_COMMAND:
                            break;
                        case TWITCH_USER_INFO:
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }
    }

    private void openUri(URI uri) {
        try {
            // Fuck reflecting awt stuff (@Mojang)
            if (java.awt.Desktop.isDesktopSupported())
                java.awt.Desktop.getDesktop().browse(uri);
            else
                WikiTool.log.error("Couldn\'t open link");
        } catch (Throwable throwable) {
            WikiTool.log.error("Couldn\'t open link", throwable);
        }
    }

    protected void drawLinkHighlight(List<String> text, int x, int y,
            FontRenderer renderer) {
        GL11.glPushMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        int k = 0;
        Iterator<String> iterator = text.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            int l = renderer.getStringWidth(s);

            if (l > k) {
                k = l;
            }
        }

        int j2 = x + 12;
        int k2 = y - 12;
        int i1 = 8;

        if (text.size() > 1) {
            i1 += 2 + (text.size() - 1) * 10;
        }

        if (j2 + k > this.width) {
            j2 -= 28 + k;
        }

        if (k2 + i1 + 6 > this.height) {
            k2 = this.height - i1 - 6;
        }

        float tempZ = this.zLevel;
        this.zLevel = 300.0F;
        int j1 = -267386864;
        this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
        this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1,
                j1);
        this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
        this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
        this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1,
                j1);
        int k1 = 1347420415;
        int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
        this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1,
                k1, l1);
        this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3
                - 1, k1, l1);
        this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
        this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1,
                l1);

        for (int i2 = 0; i2 < text.size(); ++i2) {
            String s1 = (String) text.get(i2);
            renderer.drawStringWithShadow(s1, j2, k2, -1);

            if (i2 == 0) {
                k2 += 2;
            }

            k2 += 10;
        }

        this.zLevel = tempZ;
        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    public static int argb(int A, int R, int G, int B) {
        byte[] colorByteArr = { (byte) A, (byte) R, (byte) G, (byte) B };
        return byteArrToInt(colorByteArr);
    }

    public static final int byteArrToInt(byte[] colorByteArr) {
        return (colorByteArr[0] << 24) + ((colorByteArr[1] & 0xFF) << 16)
                + ((colorByteArr[2] & 0xFF) << 8) + (colorByteArr[3] & 0xFF);
    }

}
