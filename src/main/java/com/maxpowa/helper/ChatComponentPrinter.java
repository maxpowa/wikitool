package com.maxpowa.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.sweble.wikitext.lazy.encval.IllegalCodePoint;
import org.sweble.wikitext.lazy.parser.Bold;
import org.sweble.wikitext.lazy.parser.DefinitionDefinition;
import org.sweble.wikitext.lazy.parser.DefinitionList;
import org.sweble.wikitext.lazy.parser.DefinitionTerm;
import org.sweble.wikitext.lazy.parser.Enumeration;
import org.sweble.wikitext.lazy.parser.EnumerationItem;
import org.sweble.wikitext.lazy.parser.ExternalLink;
import org.sweble.wikitext.lazy.parser.HorizontalRule;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.sweble.wikitext.lazy.parser.Italics;
import org.sweble.wikitext.lazy.parser.Itemization;
import org.sweble.wikitext.lazy.parser.ItemizationItem;
import org.sweble.wikitext.lazy.parser.MagicWord;
import org.sweble.wikitext.lazy.parser.Paragraph;
import org.sweble.wikitext.lazy.parser.Section;
import org.sweble.wikitext.lazy.parser.SemiPre;
import org.sweble.wikitext.lazy.parser.SemiPreLine;
import org.sweble.wikitext.lazy.parser.Signature;
import org.sweble.wikitext.lazy.parser.Table;
import org.sweble.wikitext.lazy.parser.TableCaption;
import org.sweble.wikitext.lazy.parser.TableCell;
import org.sweble.wikitext.lazy.parser.TableHeader;
import org.sweble.wikitext.lazy.parser.TableRow;
import org.sweble.wikitext.lazy.parser.Ticks;
import org.sweble.wikitext.lazy.parser.Url;
import org.sweble.wikitext.lazy.parser.Whitespace;
import org.sweble.wikitext.lazy.parser.XmlElement;
import org.sweble.wikitext.lazy.parser.XmlElementClose;
import org.sweble.wikitext.lazy.parser.XmlElementEmpty;
import org.sweble.wikitext.lazy.parser.XmlElementOpen;
import org.sweble.wikitext.lazy.preprocessor.Redirect;
import org.sweble.wikitext.lazy.preprocessor.TagExtension;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;
import org.sweble.wikitext.lazy.preprocessor.TemplateParameter;
import org.sweble.wikitext.lazy.preprocessor.XmlComment;
import org.sweble.wikitext.lazy.utils.XmlAttribute;
import org.sweble.wikitext.lazy.utils.XmlAttributeGarbage;
import org.sweble.wikitext.lazy.utils.XmlCharRef;
import org.sweble.wikitext.lazy.utils.XmlEntityRef;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import de.fau.cs.osr.utils.StringUtils;

public class ChatComponentPrinter extends AstVisitor {
    private IChatComponent out = new ChatComponentText("");
    private ChatStyle currentStyle = new ChatStyle();

    private Stack<String> indent = new Stack<String>();

    /** At the start of the document we somewhat had a "newline" */
    private boolean needIndent = true;

    @Override
    protected Object after(AstNode node, Object result) {
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

    public static IChatComponent print(AstNode node, String articleTitle) {
        return print(new ChatComponentText(""), node, articleTitle);
    }

    public static IChatComponent print(ChatComponentText sb, AstNode node,
            String articleTitle) {
        new ChatComponentPrinter(sb, articleTitle).go(node.get(0));
        sb.setChatStyle(new ChatStyle());
        return sb;
    }

    public void visit(AstNode astNode) throws IOException {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        // print(astNode.getClass().getSimpleName());
        // print("</span>");
    }

    public void visit(NodeList l) throws IOException {
        iterate(l);
    }

    public void visit(Text text) throws IOException {
        print(text.getContent());
    }

    public void visit(Italics n) throws IOException {
        ChatStyle style = currentStyle.createDeepCopy();

        if (currentStyle.getItalic())
            currentStyle.setItalic(false);
        else
            currentStyle.setItalic(true);

        iterate(n.getContent());

        currentStyle = style;
    }

    public void visit(Ticks t) throws IOException {
        ChatStyle style = currentStyle.createDeepCopy();

        if (currentStyle.getBold())
            currentStyle.setBold(false);
        else
            currentStyle.setBold(true);

        currentStyle = style;
    }

    public void visit(Bold n) throws IOException {
        ChatStyle style = currentStyle.createDeepCopy();

        if (currentStyle.getBold())
            currentStyle.setBold(false);
        else
            currentStyle.setBold(true);

        iterate(n.getContent());

        currentStyle = style;
    }

    public void visit(Whitespace n) throws IOException {
        iterate(n.getContent());
    }

    public void visit(Paragraph p) throws IOException {
        printNewline(false);
        renderBlockLevelElementsFirst(p);
        printNewline(false);
        if (!(isParagraphEmpty(p))) {
            printNewline(false);
            incIndent("\t");
            iterate(p.getContent());
            decIndent();
            printNewline(false);
        }
        printNewline(false);
        printNewline(false);
    }

    public void visit(SemiPre sp) throws IOException {
        printNewline(false);
        iterate(sp.getContent());
        printNewline(false);
    }

    public void visit(SemiPreLine line) throws IOException {
        iterate(line.getContent());
        print("\n");
    }

    public void visit(Section s) throws IOException {
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
        iterate(s.getTitle());

        currentStyle.setBold(false);
        incIndent("\t\t");
        iterate(s.getBody());
        decIndent();
        currentStyle = style;
    }

    public void visit(XmlComment e) throws IOException {
    }

    public void visit(XmlElement e) throws IOException {
        print("<");
        print(e.getName());
        iterate(e.getXmlAttributes());
        if (e.getEmpty().booleanValue()) {
            print(" />");
        } else {
            print(">");
            iterate(e.getBody());
            print("</");
            print(e.getName());
            print(">");
        }
    }

    public void visit(XmlAttribute a) throws IOException {
        print(" ");
        print(a.getName());
        print("=\"");
        iterate(a.getValue());
        print("\"");
    }

    public void visit(XmlAttributeGarbage g) throws IOException {
    }

    public void visit(XmlCharRef ref) throws IOException {
        print("&#");
        print(ref.getCodePoint());
        print(";");
    }

    public void visit(XmlEntityRef ref) throws IOException {
        print("&");
        print(ref.getName());
        print(";");
    }

    public void visit(DefinitionList n) throws IOException {
        printNewline(false);
        incIndent("\t");
        iterate(n.getContent());
        decIndent();
        printNewline(false);
    }

    public void visit(DefinitionTerm n) throws IOException {
        printNewline(false);
        iterate(n.getContent());
        printNewline(false);
    }

    public void visit(DefinitionDefinition n) throws IOException {
        printNewline(false);
        incIndent("\t");
        iterate(n.getContent());
        decIndent();
        printNewline(false);
    }

    public void visit(Enumeration n) throws IOException {
        printNewline(false);
        incIndent("\t");
        iterate(n.getContent());
        decIndent();
        printNewline(false);
    }

    public void visit(EnumerationItem n) throws IOException {
        printNewline(false);
        incIndent("\t");
        iterate(n.getContent());
        decIndent();
        printNewline(false);
    }

    public void visit(Itemization n) throws IOException {
        printNewline(false);
        incIndent("\t");
        iterate(n.getContent());
        decIndent();
        printNewline(false);
    }

    public void visit(ItemizationItem n) throws IOException {
        printNewline(false);
        incIndent("\t");
        iterate(n.getContent());
        decIndent();
        printNewline(false);
    }

    public void visit(ExternalLink link) throws IOException {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        StringBuilder sb = new StringBuilder(link.getTarget().getProtocol())
                .append(':').append(link.getTarget().getPath());
        currentStyle.setChatClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL, sb.toString()));
        currentStyle.setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
                        "External Link: " + sb.toString())));
        currentStyle.setUnderlined(true);
        currentStyle.setColor(EnumChatFormatting.BLUE);

        if (!(link.getTitle().isEmpty()))
            iterate(link.getTitle());
        else {
            print("\u26A1");
        }

        currentStyle = tempStyle;
    }

    public void visit(Url url) throws IOException {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        StringBuilder sb = new StringBuilder(url.getProtocol()).append(':')
                .append(url.getPath());
        currentStyle.setChatClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL, sb.toString()));
        currentStyle.setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
                        "External Link")));
        currentStyle.setUnderlined(true);
        currentStyle.setColor(EnumChatFormatting.BLUE);

        print(sb.toString());

        currentStyle = tempStyle;
    }

    public void visit(InternalLink n) throws IOException {
        ChatStyle tempStyle = currentStyle.createDeepCopy();

        currentStyle.setChatClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, makeLinkTarget(n)));
        currentStyle.setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
                        "Wiki Link: " + makeLinkTitle(n))));
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

    public void visit(Table table) throws IOException {
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

    public void visit(TableCaption caption) throws IOException {
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

    public void visit(TableRow row) throws IOException {
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

    public void visit(TableHeader header) throws IOException {
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

    public void visit(TableCell cell) throws IOException {
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

    public void visit(HorizontalRule rule) throws IOException {
        printNewline(false);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("\u2014");
        }
        print(sb.toString());
        printNewline(false);
    }

    public void visit(Signature sig) throws IOException {
        print("<span class=\"");
        print(this.classPrefix);
        print("signature\">");
        print(makeSignature(sig));
        print("</span>");
    }

    public void visit(Redirect n) throws IOException {
        print("<span class=\"");
        print(this.classPrefix);
        print("redirect\">&#x21B3; ");
        print(n.getTarget());
        print("</span>");
    }

    public void visit(IllegalCodePoint n) throws IOException {
        print("<span class=\"");
        print(this.classPrefix);
        print("illegal\">");
        print(asXmlCharRefs(n.getCodePoint()));
        print("</span>");
    }

    public void visit(MagicWord n) throws IOException {
        print("<span class=\"");
        print(this.classPrefix);
        print("magic-word\">__");
        print(n.getWord());
        print("__</span>");
    }

    public void visit(TagExtension n) throws IOException {
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

        print(n.getBody());
    }

    public void visit(XmlElementEmpty e) throws IOException {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        // print("&lt;");
        // print(e.getName());
        // iterate(e.getXmlAttributes());
        // print(" />");
        // print("</span>");
    }

    public void visit(XmlElementOpen e) throws IOException {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        // print("&lt;");
        // print(e.getName());
        // iterate(e.getXmlAttributes());
        // print(">");
        // print("</span>");
    }

    public void visit(XmlElementClose e) throws IOException {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        // print("&lt;/");
        // print(e.getName());
        // print(">");
        // print("</span>");
    }

    public void visit(Template tmpl) throws IOException {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        if (this.renderTemplates) {
            print("{");
            print("{");
            iterate(tmpl.getName());
            iterate(tmpl.getArgs());
            print("}}");
        } else if (tmpl.getArgs().isEmpty()) {
            print("{");
            print("{");
            iterate(tmpl.getName());
            print("}}");
        } else {
            print("{");
            print("{");
            iterate(tmpl.getName());
            print("|...}}");
        }

        // print("</span>");
    }

    public void visit(TemplateParameter param) throws IOException {
        // print("<span class=\"");
        // print(this.classPrefix);
        // print("unknown-node\">");
        if (this.renderTemplates) {
            print("{");
            print("{");
            print("{");
            iterate(param.getName());
            dispatch(param.getDefaultValue());
            iterate(param.getGarbage());
            print("}}}");
        } else if (param.getDefaultValue() == null) {
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

    public void visit(TemplateArgument arg) throws IOException {
        print("|");
        if (arg.getHasName()) {
            iterate(arg.getValue());
        } else {
            iterate(arg.getName());
            print("=");
            iterate(arg.getValue());
        }
    }

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
            this.classPrefix = new StringBuilder().append(this.classPrefix)
                    .append('-').toString();
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
    private void renderBlockLevelElementsFirst(Paragraph p) {
        List<AstNode> l = (List<AstNode>) p.getAttribute("blockLevelElements");
        if (l == null) {
            return;
        }
        for (AstNode n : l)
            dispatch(n);
    }

    private boolean isParagraphEmpty(Paragraph p) {
        if (!(p.isEmpty())) {
            List<?> l = (List<?>) p.getAttribute("blockLevelElements");
            if ((l == null) || (p.size() - l.size() > 0))
                return false;
        }
        return true;
    }

    private String makeLinkTitle(InternalLink n) {
        return n.getTarget();
    }

    private String makeLinkTarget(InternalLink n) {
        return n.getTarget();
    }

    private String makeSignature(Signature sig) {
        return "[SIG]";
    }
}