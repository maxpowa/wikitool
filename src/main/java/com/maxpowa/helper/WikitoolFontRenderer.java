package com.maxpowa.helper;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
	
	//TODO: MAKE THIS WORK
	public List listFormattedStringToWidth(IChatComponent input, int width) {
		return null;
	}

}
