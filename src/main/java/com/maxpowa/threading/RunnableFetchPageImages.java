package com.maxpowa.threading;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import org.wikipedia.Wiki;

import com.maxpowa.WikiTool;
import com.maxpowa.WikiUtil;

public class RunnableFetchPageImages implements Runnable {

    private FetchStatus status;
    private Wiki wiki;
    private String page;

    public RunnableFetchPageImages(String page, Wiki wiki) {
        this.page = page;
        this.wiki = wiki;
        this.status = FetchStatus.WAITING;
    }

    @Override
    public void run() {
        if (!WikiUtil.imageCache.containsKey(this.page)) {
            WikiUtil.imageCache.put(this.page, new HashMap<String, FetchResult>());
        }
        
        this.status = FetchStatus.DOWNLOADING;
        File cachedir = new File(Minecraft.getMinecraft().mcDataDir, "wikitool/cache/" + wiki.getDomain());
        String[] imageNames = {};
        try {
            imageNames = wiki.getImagesOnPage(page);
        } catch (IOException e) {
            WikiTool.log.error("Failed to load images from cache for " + this.page + " on " + this.wiki.getDomain(), e);
            this.status = FetchStatus.ERRORED;
        }

        HashMap<String, FetchResult> results = WikiUtil.imageCache.get(this.page);
        for (String imageName : imageNames) {
            String originalName = imageName;
            imageName = WikiUtil.sanitizeFilename(imageName);
            File imgfile = new File(cachedir, imageName);
            if (cachedir.exists()) {
                if (imgfile.exists()) {
                    try {
                        results.put(imageName, new FetchResult(imageName).setStatus(FetchStatus.DOWNLOADING));
                        results.get(imageName).setBufferedImage(ImageIO.read(imgfile));
                    } catch (IOException e) {
                        WikiTool.log.error("Failed to load image \""+imageName+"\" from cache in " + this.wiki.getDomain(), e);
                    }
                } else {
                    results.put(imageName, new FetchResult(imageName));
                }
            } else {
                cachedir.mkdirs();
                results.put(imageName, new FetchResult(imageName));
            }
            
            try {
                if (results.get(imageName).getStatus() != FetchStatus.DONE) {
                    results.get(imageName).setStatus(FetchStatus.DOWNLOADING);
                    BufferedImage temp = createImageFromBytes(wiki.getImage(originalName, 256, 256));
                    try {
                        ImageIO.write(temp, "png", imgfile);
                    } catch (IOException e) {
                        WikiTool.log.error("Failed to save "+imgfile.getAbsolutePath(), e);
                    }
                    results.get(imageName).setBufferedImage(temp);
                }
            } catch (IOException e) {
                WikiTool.log.error("Failed to fetch " + imageName + " from " + this.wiki.getDomain(), e);
            }
        }
        this.status = FetchStatus.DONE;
    }

    private BufferedImage createImageFromBytes(byte[] imageData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        return ImageIO.read(bais);
    }

    public FetchStatus getStatus() {
        return this.status;
    }

    public static enum FetchStatus {
        /**
         * Waiting for image data to be fetched
         */
        WAITING, 
        /**
         * May also indicate reading from cache
         */
        DOWNLOADING, 
        /**
         * Indicates an error state
         */
        ERRORED, 
        /**
         * Has fetched all available image data
         */
        DONE
    }

    public static class FetchResult {

        private FetchStatus status = FetchStatus.WAITING;
        private BufferedImage img = null;
        private ResourceLocation result;
        private DynamicTexture texture;

        public FetchResult(String identifier) {
            this.result = new ResourceLocation(WikiTool.MOD_ID, "cache/" + identifier);
        }

        public FetchStatus getStatus() {
            return this.status;
        }

        public ResourceLocation getResourceLocation() {
            switch (status) {
                case DONE:
                    return result;
                case ERRORED:
                    return new ResourceLocation(WikiTool.MOD_ID, "textures/gui/error.png");
                default:
                    return new ResourceLocation(WikiTool.MOD_ID, "textures/gui/ring.png");
            }
        }

        public FetchResult setStatus(FetchStatus status) {
            this.status = status;
            return this;
        }
        
        public int getWidth() {
            if (this.status != FetchStatus.DONE)
                return 256;
            return this.img.getWidth();
        }
        
        public int getHeight() {
            if (this.status != FetchStatus.DONE)
                return 256;
            return this.img.getHeight();
        }
        
        public BufferedImage getBufferedImage() {
            return this.img;
        }

        public FetchResult setBufferedImage(BufferedImage source) {
            this.texture = (DynamicTexture) Minecraft.getMinecraft().getTextureManager().getTexture(this.result);
            this.img = source;

            try {
                if (this.texture == null) {
                    this.texture = new DynamicTexture(img.getWidth(), img.getHeight());
                    Minecraft.getMinecraft().getTextureManager().loadTexture(this.result, this.texture);
                }
            } catch (RuntimeException e) {
                // Silently fail out if this isn't in the main thread, cause I don't know of a better fucking way to do it.
                this.status = FetchStatus.ERRORED;
            }

            if (this.texture != null) {
                this.img.getRGB(0, 0, img.getWidth(), img.getHeight(), this.texture.getTextureData(), 0, img.getWidth());
                this.texture.updateDynamicTexture();
                this.status = FetchStatus.DONE;
            }
            return this;
        }
        
        public FetchResult reloadDynamicTexture() {
            this.status = FetchStatus.DOWNLOADING;
            return setBufferedImage(this.img);
        }

        public boolean needsReload() {
            return (this.texture == null);
        }

    }

}
