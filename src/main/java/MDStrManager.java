import org.commonmark.internal.HeadingParser;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

class HeadingVisitor extends AbstractVisitor {

    private final HtmlWriter html;
    StringBuilder stringBuilder;

    HeadingVisitor() {
        stringBuilder = new StringBuilder();
        this.html = new HtmlWriter(stringBuilder);
    }


    @Override
    public void visit(Text text) {
        System.out.println(text.getLiteral() + "###" + stringBuilder.toString());
        if (text.getParent().getClass().equals(Heading.class)) {
            Heading heading = (Heading) text.getParent();
            String htag = "h" + heading.getLevel();
            this.html.line();
            this.html.tag(htag);
            this.html.text(text.getLiteral());
            this.html.tag('/' + htag);
            this.html.line();

        }

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
