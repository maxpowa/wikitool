package com.maxpowa.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maxpowa.WikiUtil.ArticleParser;

@Deprecated
public class MCWikiParser extends ArticleParser {

    @Override
    public String parse(String title, String input) {
        int i = 0;
        while (i < input.length()/6) {
            input = input.replaceFirst("'''", "\u00a7l");
            input = input.replaceFirst("'''", "\u00a7r");
            input = input.replaceFirst("''", "\u00a7o");
            input = input.replaceFirst("''", "\u00a7r");
            i++;
        }
        
        Matcher m = Pattern.compile("(\\{\\|(?:.|\\n)*?\\|\\})", Pattern.DOTALL | Pattern.MULTILINE).matcher(input);
        
        for (int t = 0; t < 10 && m.find(); t++) {
            new WikiTableParser(title+"#"+(t), m.group(1)).parse();
        }
        input = m.replaceAll("");
        input = input.replaceAll("\\{\\{cleanup\\}\\}\\n", "").replaceAll("\\{\\{[mM]inecraft\\}\\}\\n", "").replaceAll("\\[\\[(?=[^FILE])", "\u00a73").replaceAll("\\[\\[", "").replaceAll("\\]\\]", "\u00a7f");
        return input;
    }

}
