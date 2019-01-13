import org.commonmark.internal.HeadingParser;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class HeadingVisitor extends AbstractVisitor {

    private final HtmlWriter html;
    StringBuilder stringBuilder;

    HeadingVisitor() {
        stringBuilder = new StringBuilder();
        this.html = new HtmlWriter(stringBuilder);
    }

    @Override
    public void visit(Heading heading) {
        super.visit(heading);
        String htag = "h" + heading.getLevel();
        this.html.line();
        this.html.tag(htag);
        this.visitChildren(heading);
        this.html.tag('/' + htag);
        this.html.line();
    }

    @Override
    public void visit(Text text) {
        // This is called for all Text nodes. Override other visit methods for other node types.

        // Count words (this is just an example, don't actually do it this way for various reasons).
        System.out.println(text.getLiteral()+"###"+stringBuilder.toString());
        if (text.getParent().getClass().equals(Heading.class))
            this.html.text(text.getLiteral());
        // Descend into children (could be omitted in this case because Text nodes don't have children).
//        visitChildren(text);
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }
}


public class MDStrManager {
    private String mdStr;
    private String htmlStr;
    private String outlineStr;
    private Parser parser;
    private HtmlRenderer renderer;
    private HtmlRenderer outlineRender;
    private boolean isDirty;

    public MDStrManager() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
        isDirty = false;
    }

    public void setMdStr(String newStr) {
        isDirty = true;
        mdStr = newStr;
        Node document = parser.parse(mdStr);
        htmlStr = renderer.render(document);
        HeadingVisitor headingVisitor = new HeadingVisitor();
        document.accept(headingVisitor);
        outlineStr = headingVisitor.toString();
        System.out.println(outlineStr);
        System.out.println("===================");
    }

    public String getMdStr() {
        return mdStr;
    }

    public String getHtmlStr() {
        return htmlStr;
    }

    public String getOutlineStr() {
        return outlineStr;
    }


    public boolean isDirty() {
        return isDirty;
    }

    public void done() {
        isDirty = false;
    }


}
