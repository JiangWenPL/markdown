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

class HeadingNodeRenderer implements NodeRenderer {

    private final HtmlWriter html;

    HeadingNodeRenderer(HtmlNodeRendererContext context) {
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet(Arrays.asList(Document.class, Heading.class, Paragraph.class, BlockQuote.class, BulletList.class, FencedCodeBlock.class, HtmlBlock.class, ThematicBreak.class, IndentedCodeBlock.class, Link.class, ListItem.class, OrderedList.class, Image.class, Emphasis.class, StrongEmphasis.class, Text.class, Code.class, HtmlInline.class, SoftLineBreak.class, HardLineBreak.class));
    }

    @Override
    public void render(Node node) {
        // We only handle one type as per getNodeTypes, so we can just cast it here.
        System.out.println(node.getClass() + " VS " + Heading.class);
        if (node.getClass().equals(Heading.class)) {
            Heading heading = (Heading) node;
            html.line();
            html.tag("h" + heading.getLevel());
            html.tag("/h" + heading.getLevel());
            html.line();
        }
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
        outlineRender = HtmlRenderer.builder()
                .nodeRendererFactory(HeadingNodeRenderer::new)
                .build();
        isDirty = false;
    }

    public void setMdStr(String newStr) {
        isDirty = true;
        mdStr = newStr;
        Node document = parser.parse(mdStr);
        htmlStr = renderer.render(document);
        outlineStr = outlineRender.render(document);
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
