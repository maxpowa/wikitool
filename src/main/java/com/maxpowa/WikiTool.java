package com.maxpowa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.wikipedia.Wiki;

import com.maxpowa.helper.MCWikiParser;
import com.maxpowa.threading.RunnableUpdateCheck;
import com.maxpowa.ui.GuiWikiButton;
import com.maxpowa.ui.GuiWikiScreen;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Type;

@Mod(modid = "WikiTool", name = "WikiTool", version = "v@VERSION@")
public class WikiTool {

    public static ArrayList<Wiki> wikis = new ArrayList<Wiki>();
    public static Logger log;
    private GuiWikiButton button = new GuiWikiButton(5, 2);
    protected static TreeMap<String, String> guiEquivs = new TreeMap<String, String>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        WikiUtil.log = log;
        new Thread(new RunnableUpdateCheck(1055, event.getModMetadata().version)).start();
        log.info("Starting initialization of default wikis");
        Wiki ftbWiki = new Wiki("ftbwiki.org", "");
        ftbWiki.setUsingCompressedRequests(false);
        Wiki mcWiki = new Wiki("minecraft.gamepedia.com", "");
        mcWiki.setUsingCompressedRequests(false);
        wikis.add(mcWiki);
        wikis.add(ftbWiki);
        WikiUtil.registerParser(new MCWikiParser());
        log.info("Finished initializing " + wikis.size() + " wikis.");
        log.info("Loading vanilla gui wiki pages");
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/assets/wikitool/guiwikiequiv.txt")));
        String line = "";
        try {
            while ((line = in.readLine()) != null) {
                String[] split = line.split("=");
                guiEquivs.put(split[0], split[1]);
            }
            log.info("Done loading " + guiEquivs.size() + " pages.");
        } catch (IOException e) {
            log.error("Error loading vanilla gui wiki pages. Skipping.");
        }
    }

    public static String getEquiv(Object clazz) {
        if (guiEquivs.containsKey(clazz.getClass().getName()))
            return guiEquivs.get(clazz.getClass().getName());
        else
            return clazz.getClass().getName();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (String s : guiEquivs.keySet()) {
            log.info(s + " : " + guiEquivs.get(s));
        }
    }

    @SubscribeEvent
    public void RenderTickEvent(RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if ((event.type == Type.RENDER || event.type == Type.CLIENT) && event.phase == Phase.END && !(mc.currentScreen instanceof GuiWikiScreen) && mc.currentScreen != null) { // &&
                                                                                                                                                                                // mc.currentScreen
                                                                                                                                                                                // instanceof
                                                                                                                                                                                // GuiMainMenu)
                                                                                                                                                                                // {
            int mouseX = Mouse.getX() * mc.currentScreen.width / mc.displayWidth;
            int mouseY = mc.currentScreen.height - Mouse.getY() * mc.currentScreen.height / mc.displayHeight - 1;
            button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }
    }

    public static void main(String[] args) {
        try {
            for (Wiki wiki : wikis) {
                System.out.println(wiki.getSiteInfo().get("Title"));
                for (String s : wiki.getAllCategories()) {
                    System.out.println(s);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
