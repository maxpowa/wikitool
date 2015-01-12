package com.maxpowa.threading;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sweble.wikitext.parser.WikitextParser;
import org.sweble.wikitext.parser.WikitextPostprocessor;
import org.wikipedia.Wiki;

import xtc.parser.ParseException;

import com.maxpowa.WikiTool;
import com.maxpowa.WikiUtil;
import com.maxpowa.WikiUtil.FetchState;
import com.maxpowa.helper.WikitextParserConfig;

public class RunnableFetchPage implements Runnable {

    String page;
    String finalString;

    public RunnableFetchPage(String page) {
        this.page = page;
        finalString = "";
    }

    public void run() {
        WikiUtil.state = FetchState.SEARCHING;
        WikitextParser lp = new WikitextParser(new WikitextParserConfig());
        WikitextPostprocessor wtpp = new WikitextPostprocessor(new WikitextParserConfig());
        for (Wiki wiki : WikiTool.wikis) {
            WikiUtil.state = FetchState.READING;
            try {
                WikiTool.log.info(this.page + " found on " + wiki.getDomain());
                WikiUtil.state = FetchState.PARSING;
                WikiUtil.cache.put(wiki.getDomain() + "-" + page, wtpp.postprocess(lp.parseArticle(wiki.getPageText(page), page), page));
                Thread fetchImageThread = new Thread(new RunnableFetchPageImages(page, wiki));
                fetchImageThread.start();
            } catch (FileNotFoundException e) {
                WikiTool.log.info(this.page + " was not found on " + wiki.getDomain());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        WikiUtil.resetFetch();
        WikiUtil.state = FetchState.DONE;
        return;
    }

}
