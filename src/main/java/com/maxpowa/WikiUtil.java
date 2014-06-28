package com.maxpowa;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;

import com.maxpowa.helper.WikiTableParser.WikiTable;
import com.maxpowa.threading.RunnableFetchPage;

import de.fau.cs.osr.ptk.common.ast.AstNode;


public class WikiUtil {

    private static Thread fetchPageThread = null;
    public static FetchState state = FetchState.NONE;
    private static ArrayList<ArticleParser> parsers = new ArrayList<ArticleParser>();
    protected static Logger log;
    
    public static TreeMap<String, AstNode> cache = new TreeMap<String, AstNode>();
    public static TreeMap<String, WikiTable> tables = new TreeMap<String, WikiTable>();
    
    public static AstNode getPage(String page, String domain) {
        if (page.contains("#"))
            page = page.split("#")[0];
        
        if (cache.containsKey(domain+"-"+page)) {
            return cache.get(domain+"-"+page);
        } else if (state == FetchState.DONE || state == FetchState.NONE) {
            fetchPage(page);
            return null;
        } else {
            return null;
        }
    }
    
    public static void registerParser(ArticleParser parser) {
        parsers.add(parser);
    }
    
    public static ArrayList<ArticleParser> getParsers() {
        return parsers;
    }
    
    public static AstNode getLastPage() {
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
    
    public enum FetchState {NONE,SEARCHING,READING,PARSING,DONE};
    
    public static abstract class ArticleParser {

        public abstract String parse(String title, String input);
        
    }
}
