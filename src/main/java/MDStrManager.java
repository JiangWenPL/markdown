import name.fraser.neil.plaintext.diff_match_patch;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.LinkedList;

class HeadingVisitor extends AbstractVisitor {

    private final HtmlWriter html;
    StringBuilder stringBuilder;

    HeadingVisitor() {
        stringBuilder = new StringBuilder();
        this.html = new HtmlWriter(stringBuilder);
    }


    @Override
    public void visit(Text text) {
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
    private diff_match_patch merger;
    public long lastModifiedTime;

    public MDStrManager() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
        isDirty = false;
        merger = new diff_match_patch();
        lastModifiedTime = System.currentTimeMillis();
    }

    public void setMdStr(String newStr) {
        isDirty = true;
        mdStr = newStr;
        Node document = parser.parse(mdStr);
        htmlStr = renderer.render(document);
        HeadingVisitor headingVisitor = new HeadingVisitor();
        document.accept(headingVisitor);
        outlineStr = headingVisitor.toString();
        lastModifiedTime = System.currentTimeMillis();
    }

    public void mergeMdStr(String remoteStr) {
        LinkedList<diff_match_patch.Patch> patches = merger.patch_make(this.mdStr, remoteStr);
        if (this.mdStr == null)
            this.mdStr = "";
        if (remoteStr == null)
            remoteStr = "";
        String mergedText;
        Object object[] = merger.patch_apply(patches, this.mdStr);
        mergedText = (String) object[0];
        System.out.println("Merge done: " + this.mdStr + " VS " + remoteStr + " -> " + mergedText);
        this.setMdStr(mergedText);
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
