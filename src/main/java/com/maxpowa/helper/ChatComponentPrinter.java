package com.maxpowa.helper;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.sweble.wikitext.parser.nodes.WtBold;
import org.sweble.wikitext.parser.nodes.WtDefinitionList;
import org.sweble.wikitext.parser.nodes.WtDefinitionListDef;
import org.sweble.wikitext.parser.nodes.WtDefinitionListTerm;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtHorizontalRule;
import org.sweble.wikitext.parser.nodes.WtIllegalCodePoint;
import org.sweble.wikitext.parser.nodes.WtImEndTag;
import org.sweble.wikitext.parser.nodes.WtImStartTag;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtListItem;
import org.sweble.wikitext.parser.nodes.WtNewline;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtOrderedList;
import org.sweble.wikitext.parser.nodes.WtPageSwitch;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtRedirect;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtSemiPre;
import org.sweble.wikitext.parser.nodes.WtSemiPreLine;
import org.sweble.wikitext.parser.nodes.WtSignature;
import org.sweble.wikitext.parser.nodes.WtTable;
import org.sweble.wikitext.parser.nodes.WtTableCaption;
import org.sweble.wikitext.parser.nodes.WtTableCell;
import org.sweble.wikitext.parser.nodes.WtTableHeader;
import org.sweble.wikitext.parser.nodes.WtTableRow;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateParameter;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtTicks;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
import org.sweble.wikitext.parser.nodes.WtXmlAttribute;
import org.sweble.wikitext.parser.nodes.WtXmlAttributeGarbage;
import org.sweble.wikitext.parser.nodes.WtXmlCharRef;
import org.sweble.wikitext.parser.nodes.WtXmlComment;
import org.sweble.wikitext.parser.nodes.WtXmlElement;
import org.sweble.wikitext.parser.nodes.WtXmlEntityRef;

import com.maxpowa.WikiTool;
import com.maxpowa.templates.KeyTemplate;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;

public class ChatComponentPrinter extends AstVisitor<WtNode> {
    private IChatComponent out = new ChatComponentText("");
    private ChatStyle currentStyle = new ChatStyle();

    private Stack<String> indent = new Stack<String>();

    /** At the start of the document we somewhat had a "newline" */
    private boolean needIndent = true;

    @Override
    protected Object after(WtNode node, Object result) {
        // Run any cleanup/stream close things
        return super.after(node, result);
    }

    // =========================================================================

    protected void incIndent(String inc) {
        indent.push(indent.peek() + inc.replace("\t", "    "));
    }

    protected void decIndent() {
        indent.pop();
    }

    protected void print(String text) {
        if (text == null)
            return;

        if (needIndent) {
            out.appendText(indent.peek());
            needIndent = false;
        }

        ChatComponentText txt = new ChatComponentText(text);
        txt.setChatStyle(currentStyle.createDeepCopy());

        out.appendSibling(txt.createCopy());
    }

    protected void print(int number) {
        print(String.valueOf(number));
    }

    protected void printNewline(boolean force) {
        // we already had a newline -> collapse
        if (!needIndent || force) {
            out.appendText("\n");
            needIndent = true;
        }
    }

    protected String indentText(String text) {
        return StringUtils.indent2(text, indent.peek());
    }

    protected static String camelCaseToUppercase(String name) {
        return StringUtils.camelcaseToUppercase(name);
    }

    protected static String startWithUppercase(String name) {
        return StringUtils.startWithUppercase(name);
    }

    protected static String strrep(char ch, int rep) {
        return StringUtils.strrep(ch, rep);
    }

    protected static String strrep(String str, int rep) {
        return StringUtils.strrep(str, rep);
    }

    // //// End basic printer methods

    private String classPrefix;
    // private String articleTitle = "";

    private boolean renderTemplates = false;

    public static IChatComponent print(WtNode node, String articleTitle) {
        return print(new ChatComponentText(""), node, articleTitle);
    }

    public static IChatComponent print(ChatComponentText sb, WtNode node, String articleTitle) {
        new ChatComponentPrinter(sb, articleTitle).go(node);
        sb.setChatStyle(new ChatStyle());
        return sb;
    }

    public void visit(WtNode n) {
        // Fallback for all nodes that are not explicitly handled below
        print("*");
        print(n.getNodeName());
        print("*");
    }
    
    public void visit(WtImStartTag n) {
        // NO-OP
    }
    
    public void visit(WtImEndTag n) {
        // NO-OP
    }
    
    public void visit(WtNodeList n) {
        iterate(n);
    }

    Pattern template = Pattern.compile("\\{\\{(.+?)(?:[|](.+?))?\\}\\}");

    public void visit(WtText text) {
        print(text.getContent());
    }

    public void visit(WtNewline n) {
        printNewline(true);
    }

    public void visit(WtItalics n) {
        ChatStyle style = currentStyle.createDeepCopy();

        if (currentStyle.getItalic())
            currentStyle.setItalic(false);
        else
            currentStyle.setItalic(true);

        iterate(n);

        currentStyle = style;
    }

    public void visit(WtTicks t) {
        ChatStyle style = currentStyle.createDeepCopy();

        if (currentStyle.getBold())
            currentStyle.setBold(false);
        else
            currentStyle.setBold(true);

        currentStyle = style;
    }

    public void visit(WtBold n) {
        ChatStyle style = currentStyle.createDeepCopy();

        if (currentStyle.getBold())
            currentStyle.setBold(false);
        else
            currentStyle.setBold(true);

        iterate(n);

        currentStyle = style;
    }

    public void visit(WtWhitespace n) {
        iterate(n);
    }

    public void visit(WtParagraph p) {
        printNewline(false);
        renderBlockLevelElementsFirst(p);
        printNewline(false);
        if (!(isParagraphEmpty(p))) {
            printNewline(false);
            incIndent("\t");
            iterate(p);
            decIndent();
            printNewline(false);
        }
        printNewline(false);
        printNewline(false);
    }

    public void visit(WtSemiPre sp) {
        printNewline(false);
        iterate(sp);
        printNewline(false);
    }

    public void visit(WtSemiPreLine line) {
        iterate(line);
        print("\n");
    }

    public void visit(WtSection s) {
        // printNewline(false);
        // print("<div class=\"");
        // print(this.classPrefix);
        // print("section\">");
        // printNewline(true);
        // print("\t<h");
        // print(s.getLevel());
        // print(">");
        // iterate(s.getTitle());
        // print("</h");
        // print(s.getLevel());
        // print(">");
        // printNewline(true);
        // print("\t<div class=\"");
        // print(this.classPrefix);
        // print("section-body\">");
        // printNewline(false);
        // incIndent("\t\t");
        // iterate(s.getBody());
        // decIndent();
        // printNewline(false);
        // print("\t</div>");
        // printNewline(true);
        // print("</div>");
        // printNewline(false);

        ChatStyle style = currentStyle.createDeepCopy();
        currentStyle.setBold(true);
        iterate(s.getHeading());

        currentStyle.setBold(false);
        printNewline(false);
        print("------");

        incIndent("\t\t");
        iterate(s.getBody());
        decIndent();
        currentStyle = style;
    }

    public void visit(WtXmlComment e) {
    }

    public void visit(WtXmlElement e) {
        print("<");
        print(e.getName());
        iterate(e.getXmlAttributes());
        if (e.isEmpty()) {
            print(" />");
        } else {
            print(">");
            iterate(e.getBody());
            print("</");
            print(e.getName());
            print(">");
        }
    }

    public void visit(WtXmlAttribute a) {
        print(" ");
        print(a.getName().getAsString());
        print("=\"");
        iterate(a.getValue());
        print("\"");
    }

    public void visit(WtXmlAttributeGarbage g) {
    }

    public void visit(WtXmlCharRef ref) {
        print("&#");
        print(ref.getCodePoint());
        print(";");
    }

    public void visit(WtXmlEntityRef ref) {
        print("&");
        print(ref.getName());
        print(";");
    }

    public void visit(WtDefinitionList n) {
        printNewline(false);
        incIndent("\t");
        iterate(n);
        decIndent();
        printNewline(false);
    }

    public void visit(WtDefinitionListTerm n) {
        printNewline(false);
        iterate(n);
        printNewline(false);
    }

    public void visit(WtDefinitionListDef n) {
        printNewline(false);
        incIndent("\t");
        iterate(n);
        decIndent();
        printNewline(false);
    }

    public void visit(WtUnorderedList n) {
        printNewline(false);
        incIndent("\t");
        iterate(n);
        decIndent();
        printNewline(false);
    }

    public void visit(WtOrderedList n) {
        printNewline(false);
        incIndent("\t");
        iterate(n);
        decIndent();
        printNewline(false);
    }

    public void visit(WtListItem n) {
        printNewline(false);
        incIndent("\t");
        iterate(n);
        decIndent();
        printNewline(false);
    }
    
    public void visit(WtImageLink n) {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        currentStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, n.getTarget().getAsString()));
        currentStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Wiki Link: " + n.getAlt())));
        currentStyle.setUnderlined(true);
        currentStyle.setColor(EnumChatFormatting.DARK_AQUA);

        print(n.getTarget().getAsString());

        currentStyle = tempStyle;
    }

    public void visit(WtExternalLink link) {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        StringBuilder sb = new StringBuilder(link.getTarget().getProtocol()).append(':').append(link.getTarget().getPath());
        currentStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sb.toString()));
        currentStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("External Link: " + sb.toString())));
        currentStyle.setUnderlined(true);
        currentStyle.setColor(EnumChatFormatting.BLUE);

        if (!(link.getTitle().isEmpty()))
            iterate(link.getTitle());
        else {
            print("\u26A1");
        }

        currentStyle = tempStyle;
    }

    public void visit(WtUrl url) {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        StringBuilder sb = new StringBuilder(url.getProtocol()).append(':').append(url.getPath());
        currentStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sb.toString()));
        currentStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("External Link")));
        currentStyle.setUnderlined(true);
        currentStyle.setColor(EnumChatFormatting.BLUE);

        print(sb.toString());

        currentStyle = tempStyle;
    }

    public void visit(WtInternalLink n) {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        currentStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, makeLinkTarget(n)));
        currentStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Wiki Link: " + makeLinkTitle(n))));
        currentStyle.setUnderlined(true);
        currentStyle.setColor(EnumChatFormatting.BLUE);

        print(makeLinkTarget(n));

        currentStyle = tempStyle;

        // print("<a href=\"");
        // print(makeLinkTarget(n));
        // print("\">");
        // print(n.getPrefix());
        // if (n.getTitle().getContent().isEmpty())
        // print(makeLinkTitle(n));
        // else {
        // iterate(n.getTitle().getContent());
        // }
        // print(n.getPostfix());
        // print("</a>");
    }

    public void visit(WtTable table) {
        // printNewline(false);
        // print("<table");
        // iterate(table.getXmlAttributes());
        // print(">");
        // printNewline(false);
        // incIndent("\t");
        // iterate(table.getBody());
        // decIndent();
        // printNewline(false);
        // printNewline(true);
        // print("</table>");
        // printNewline(false);
        incIndent("\t");
        iterate(table.getBody());
        decIndent();
    }

    public void visit(WtTableCaption caption) {
        // printNewline(false);
        // print("<caption");
        // iterate(caption.getXmlAttributes());
        // print(">");
        // printNewline(false);
        // incIndent("\t");
        // iterate(caption.getBody());
        // decIndent();
        // printNewline(false);
        // print("</caption>");
        // printNewline(false);
        incIndent("\t");
        iterate(caption.getBody());
        decIndent();
    }

    public void visit(WtTableRow row) {
        // printNewline(false);
        // print("<tr");
        // iterate(row.getXmlAttributes());
        // print(">");
        // printNewline(false);
        // incIndent("\t");
        // iterate(row.getBody());
        // decIndent();
        // printNewline(false);
        // print("</tr>");
        // printNewline(false);
        incIndent("\t");
        iterate(row.getBody());
        decIndent();
    }

    public void visit(WtTableHeader header) {
        // printNewline(false);
        // print("<th");
        // iterate(header.getXmlAttributes());
        // print(">");
        // printNewline(false);
        // incIndent("\t");
        // iterate(header.getBody());
        // decIndent();
        // printNewline(false);
        // print("</th>");
        // printNewline(false);
        incIndent("\t");
        iterate(header.getBody());
        decIndent();
    }

    public void visit(WtTableCell cell) {
        // printNewline(false);
        // print("<td");
        // iterate(cell.getXmlAttributes());
        // print(">");
        // printNewline(false);
        // incIndent("\t");
        // iterate(cell.getBody());
        // decIndent();
        // printNewline(false);
        // print("</td>");
        // printNewline(false);
        incIndent("\t");
        iterate(cell.getBody());
        decIndent();
    }

    public void visit(WtHorizontalRule rule) {
        printNewline(false);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("\u2014");
        }
        print(sb.toString());
        printNewline(false);
    }

    public void visit(WtSignature sig) {
        print("<span class=\"");
        print(this.classPrefix);
        print("signature\">");
        print(makeSignature(sig));
        print("</span>");
    }

    public void visit(WtRedirect n) {
        print("<span class=\"");
        print(this.classPrefix);
        print("redirect\">&#x21B3; ");
        print(n.getTarget().getAsString());
        print("</span>");
    }

    public void visit(WtIllegalCodePoint n) {
        print("<span class=\"");
        print(this.classPrefix);
        print("illegal\">");
        print(asXmlCharRefs(n.getCodePoint()));
        print("</span>");
    }

    public void visit(WtPageSwitch n) {
        print("<span class=\"");
        print(this.classPrefix);
        print("magic-word\">__");
        print(n.getName());
        print("__</span>");
    }

    public void visit(WtTagExtension n) {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        // if (this.renderTagExtensions) {
        // if (n.getBody().isEmpty()) {
        // print("&lt;");
        // print(n.getName());
        // iterate(n.getXmlAttributes());
        // print(" />");
        // } else {
        // print("&lt;");
        // print(n.getName());
        // iterate(n.getXmlAttributes());
        // print(">");
        // print(n.getBody());
        // print("&lt;/");
        // print(n.getName());
        // print(">");
        // }
        // } else {
        // if (n.getXmlAttributes().isEmpty()) {
        // print("&lt;");
        // print(n.getName());
        // } else {
        // print("&lt;");
        // print(n.getName());
        // print(" ...");
        // }
        // if (n.getBody().isEmpty()) {
        // print("/>");
        // } else {
        // print(">...&lt;/");
        // print(n.getName());
        // print(">");
        // }
        // }
        // print("</span>");

        iterate(n.getBody());
    }

    public void visit(WtTemplate tmpl) {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        print("Bumping over " + tmpl.getName().getAsString());
        if (this.renderTemplates) {
            print("{{");
            iterate(tmpl.getName());
            iterate(tmpl.getArgs());
            print("}}");
        } else if (tmpl.getArgs().isEmpty()) {
            print("{{");
            iterate(tmpl.getName());
            print("}}");
        } else {
            print("{{");
            iterate(tmpl.getName());
            print("|...}}");
        }

        // print("</span>");
    }

    public void visit(WtTemplateParameter param) {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        if (this.renderTemplates) {
            print("{");
            print("{");
            print("{");
            iterate(param.getName());
            dispatch(param.getDefault());
            iterate(param.getGarbage());
            print("}}}");
        } else if (param.getDefault() == null) {
            print("{");
            print("{");
            print("{");
            iterate(param.getName());
            print("}}}");
        } else {
            print("{");
            print("{");
            print("{");
            iterate(param.getName());
            print("|...}}}");
        }

        // print("</span>");
    }

    public void visit(WtTemplateArgument arg) {
        print("|");
        if (arg.hasName()) {
            iterate(arg.getValue());
        } else {
            iterate(arg.getName());
            print("=");
            iterate(arg.getValue());
        }
    }

    // =========================================================================

    public ChatComponentPrinter(IChatComponent sb, String articleTitle) {
        this.out = sb;
        this.currentStyle = sb.getChatStyle();
        this.indent.push("");

        // this.articleTitle = articleTitle;
    }

    protected void setClassPrefix(String classPrefix) {
        if (classPrefix == null)
            return;
        this.classPrefix = classPrefix;
        if (!(classPrefix.isEmpty())) {
            this.classPrefix = new StringBuilder().append(this.classPrefix).append('-').toString();
        }
    }

    public void setRenderTemplates(boolean renderTemplates) {
        this.renderTemplates = renderTemplates;
    }

    private String asXmlCharRefs(String codePoint) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < codePoint.length(); ++i) {
            b.append("&#");
            b.append(codePoint.charAt(i));
            b.append(";");
        }
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    private void renderBlockLevelElementsFirst(WtParagraph p) {
        List<WtNode> l = (List<WtNode>) p.getAttribute("blockLevelElements");
        if (l == null) {
            return;
        }
        for (WtNode n : l)
            dispatch(n);
    }

    private boolean isParagraphEmpty(WtParagraph p) {
        if (!(p.isEmpty())) {
            List<?> l = (List<?>) p.getAttribute("blockLevelElements");
            if ((l == null) || (p.size() - l.size() > 0))
                return false;
        }
        return true;
    }

    private String makeLinkTitle(WtInternalLink n) {
        return n.getTarget().getAsString();
    }

    private String makeLinkTarget(WtInternalLink n) {
        return n.getTarget().getAsString();
    }

    private String makeSignature(WtSignature sig) {
        return "[SIG]";
    }
}