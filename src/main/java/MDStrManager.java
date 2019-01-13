import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MDStrManager {
    private String mdStr;
    private String htmlStr;
    private String outlineStr;
    private Parser parser;
    private HtmlRenderer renderer;
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
