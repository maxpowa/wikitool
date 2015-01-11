package com.maxpowa.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sweble.wikitext.parser.ParserConfig;
import org.sweble.wikitext.parser.WikitextWarning.WarningSeverity;
import org.sweble.wikitext.parser.nodes.WikitextNodeFactory;
import org.sweble.wikitext.parser.nodes.WikitextNodeFactoryImpl;
import org.sweble.wikitext.parser.parser.LinkBuilder.LinkType;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.sweble.wikitext.parser.parser.LinkTargetParser;
import org.sweble.wikitext.parser.utils.AstTextUtils;
import org.sweble.wikitext.parser.utils.AstTextUtilsImpl;

public class WikitextParserConfig implements ParserConfig {
    private final boolean warningsEnabled;
    private final boolean gatherRtd;
    private final boolean autoCorrect;
    private final WikitextNodeFactory nodeFactory;

    private final AstTextUtilsImpl textUtils;

    // =========================================================================

    public WikitextParserConfig() {
        this(true, true, false);
    }

    public WikitextParserConfig(boolean warningsEnabled, boolean gatherRtd, boolean autoCorrect) {
        this.warningsEnabled = warningsEnabled;
        this.gatherRtd = gatherRtd;
        this.autoCorrect = autoCorrect;
        this.nodeFactory = new WikitextNodeFactoryImpl(this);
        this.textUtils = new AstTextUtilsImpl(this);
    }

    // ==[ Parser features ]====================================================

    @Override
    public boolean isWarningsEnabled() {
        return warningsEnabled;
    }

    @Override
    public boolean isWarningLevelEnabled(WarningSeverity severity) {
        return true;
    }

    @Override
    public boolean isAutoCorrect() {
        return autoCorrect;
    }

    @Override
    public boolean isGatherRtData() {
        return gatherRtd;
    }

    // ==[ AST creation ]=======================================================

    @Override
    public WikitextNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    @Override
    public AstTextUtils getAstTextUtils() {
        return textUtils;
    }

    // ==[ Link classification and parsing ]====================================

    @Override
    public boolean isUrlProtocol(String proto) {
        return "http://".equalsIgnoreCase(proto) || "https://".equalsIgnoreCase(proto) || "mail:".equalsIgnoreCase(proto);
    }

    @Override
    public String getInternalLinkPrefixPattern() {
        // Doesn't make that much sense, but needed for testing ...
        return "[äöüßa-z]+";
    }

    @Override
    public String getInternalLinkPostfixPattern() {
        return "[äöüßa-z]+";
    }

    @Override
    public LinkType classifyTarget(String target) {
        LinkTargetParser ltp = new LinkTargetParser();
        try {
            ltp.parse(this, target);
        } catch (LinkTargetException e) {
            return LinkType.INVALID;
        }

        String ns = ltp.getNamespace();
        if ("file".equalsIgnoreCase(ns) || "image".equalsIgnoreCase(ns))
            return LinkType.IMAGE;

        return LinkType.PAGE;
    }

    @Override
    public boolean isNamespace(String name) {
        // keep it simple ...
        return "image".equalsIgnoreCase(name) || "file".equalsIgnoreCase(name) || "template".equals(name) || "media".equals(name) || "category".equals(name);
    }

    @Override
    public boolean isTalkNamespace(String resultNs) {
        return resultNs.toLowerCase().equals("talk");
    }

    @Override
    public boolean isInterwikiName(String name) {
        String[] interwikis = {"minecraftwiki", "wikipedia", "mediawiki"};
        for (String wiki : interwikis)
            if (wiki.equalsIgnoreCase(name))
                return true;
        return false;
    }

    @Override
    public boolean isIwPrefixOfThisWiki(String iwPrefix) {
        return false;
    }

    // ==[ Names ]==============================================================

    @Override
    public boolean isValidPageSwitchName(String word) {
        return "NOTOC".equalsIgnoreCase(word);
    }

    @Override
    public boolean isValidExtensionTagName(String name) {
        String[] tagext = {"ref", "pre", "nowiki", "gallery", "includeonly", "noinclude", "onlyinclude", "key"};
        for (String ext : tagext)
            if (ext.equalsIgnoreCase(name))
                return true;
        return false;
    }

    public boolean isRedirectKeyword(String keyword) {
        return keyword.equalsIgnoreCase("#redirect");
    }

    // ==[ Parsing XML elements ]===============================================

    @Override
    public boolean isValidXmlEntityRef(String name) {
        return true;
    }

    @Override
    public String resolveXmlEntity(String name) {
        // keep it simple ...
        if ("amp".equalsIgnoreCase(name)) {
            return "&";
        } else if ("lt".equalsIgnoreCase(name)) {
            return "<";
        } else if ("gt".equalsIgnoreCase(name)) {
            return ">";
        } else if ("nbsp".equalsIgnoreCase(name)) {
            return "\u00A0";
        } else if ("middot".equalsIgnoreCase(name)) {
            return "\u00B7";
        } else if ("mdash".equalsIgnoreCase(name)) {
            return "\u2014";
        } else if ("ndash".equalsIgnoreCase(name)) {
            return "\u2013";
        } else if ("equiv".equalsIgnoreCase(name)) {
            return "\u2261";
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> getXmlEntities() {
        return Collections.emptyMap();
    }

    // ==[ Language Conversion Tags ]===========================================

    private static final Set<String> knownFlags = new HashSet<String>(Arrays.asList("A", "T", "R", "D", "-", "H", "N"));

    public boolean isLctFlag(String flag) {
        flag = normalizeLctFlag(flag);
        return knownFlags.contains(flag);
    }

    public String normalizeLctFlag(String flag) {
        return flag.trim().toUpperCase();
    }

    private static final Set<String> knownVariants = new HashSet<String>(Arrays.asList("zh", "zh-hans", "zh-hant", "zh-cn", "zh-tw", "zh-hk", "zh-sg"));

    public boolean isLctVariant(String variant) {
        variant = normalizeLctVariant(variant);
        return knownVariants.contains(variant);
    }

    public String normalizeLctVariant(String variant) {
        return variant.trim().toLowerCase();
    }
}
