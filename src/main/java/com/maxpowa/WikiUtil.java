package com.maxpowa;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.sweble.wikitext.parser.nodes.WtNode;

import com.maxpowa.threading.RunnableFetchPage;
import com.maxpowa.threading.RunnableFetchPageImages.FetchResult;

public class WikiUtil {

    private static Thread fetchPageThread = null;
    public static FetchState state = FetchState.NONE;
    protected static Logger log;

    public static TreeMap<String, WtNode> cache = new TreeMap<String, WtNode>();
    public static ConcurrentHashMap<String, HashMap<String, FetchResult>> imageCache = new ConcurrentHashMap<String, HashMap<String, FetchResult>>();

    public static WtNode getPage(String page, String domain) {
        if (page.contains("#"))
            page = page.split("#")[0];

        if (cache.containsKey(domain + "-" + page)) {
            return cache.get(domain + "-" + page);
        } else if (state == FetchState.DONE || state == FetchState.NONE) {
            fetchPage(page);
            return null;
        } else {
            return null;
        }
    }
    
    public static FetchResult getImage(String page, String ref) {
        ref = sanitizeFilename(ref);
        if (imageCache.containsKey(page)) {
            if (imageCache.get(page).containsKey(ref)) 
                return imageCache.get(page).get(ref);
        }
        return null;
    }
    
    public static String sanitizeFilename(String filename) {
        return filename.replace("File:", "").replaceAll("(svg|jpg|gif|jpeg)$", ".png");
    }
    
    public static WtNode getLastPage() {
        if (state == FetchState.DONE)
            return cache.get(cache.lastKey());
        else
            return null;
    }

    public static void fetchPage(String page) {
        while (cache.size() > 20) {
            log.info("Removing " + cache.lastKey() + " from the cache! (Limit 20 cached pages)");
            cache.remove(cache.lastKey());
        }

        if (fetchPageThread != null) {
            log.info("Already fetching another page, please wait!");
        } else {
            fetchPageThread = new Thread(new RunnableFetchPage(page));
            fetchPageThread.start();
        }
    }

    public static void resetFetch() {
        fetchPageThread = null;
    }

    public enum FetchState {
        NONE, SEARCHING, READING, PARSING, DONE
    };

    public static abstract class ArticleParser {

        public abstract String parse(String title, String input);

    }
}
