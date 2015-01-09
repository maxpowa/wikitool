package com.maxpowa.helper;

import java.util.ArrayList;

import com.maxpowa.WikiTool;

@Deprecated
public class WikiTableParser {
    
    private String name;
    private String content;
    private WikiTable obj = new WikiTable();

    public WikiTableParser(String title, String table) {
        this.name = title;
        this.content = table;
    }
    
    public WikiTable parse() {
        //TODO ACTUALLY PARSE THE TABLE
        WikiTool.log.info("WOULD BE PARSING " + this.name + " WITH CONTENT " + this.content);
        return this.obj;
    }
    
    public class WikiTable {
        
        public int rows = 0;
        public int columns = 0;
        
        public ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
        
    }
    
}
