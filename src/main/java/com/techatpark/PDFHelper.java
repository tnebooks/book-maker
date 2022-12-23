package com.techatpark;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import io.github.furstenheim.CopyDown;

/**
 * Main Class.
 */
public class PDFHelper {

    private final Function<String, String> lineMapper;

    private final String ocrLanguage;



    public PDFHelper(Function<String, String> lineMapper, String ocrLanguage) {
        this.lineMapper = lineMapper;
        this.ocrLanguage = ocrLanguage;
    }





    public String getFrontMatter(File pdfFile, int weight) throws IOException {
        StringBuilder fronMatter = new StringBuilder("---\n");
        fronMatter.append("title: '");
        fronMatter.append(pdfFile.getName().replace(".pdf", ""));
        fronMatter.append("'\n");
        fronMatter.append("weight: ");
        fronMatter.append(weight);
        fronMatter.append("\n");
        return fronMatter.append("---\n").toString();
    }

    public String getMarkdown(File markDownFile, File pdfFile) throws IOException {
        StringBuilder mdContentBuilder = new StringBuilder();

        CopyDown converter = new CopyDown();

        String parsedHtml;

        PDFText2HTML pdfText2HTML = new PDFText2HTML();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {


            //Instantiating Splitter class
            Splitter splitter = new Splitter();

            //splitting the pages of a PDF document
            List<PDDocument> Pages = splitter.split(document);

            //Creating an iterator
            Iterator<PDDocument> iterator = Pages.listIterator();

            //Saving each page as an individual document
            int pageNumber = 0;
            while (iterator.hasNext()) {
                pageNumber++;
                PDDocument pageDocument = iterator.next();
                String md ;
                if(ocrLanguage == null) {
                    parsedHtml = pdfText2HTML.getText(pageDocument);

                    md = converter.convert(parsedHtml);
                }
                else {
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    md = converter.convert(renderer.render(parser.parse(OCRHelper.getContent(pageDocument, ocrLanguage))));
                }

                if(lineMapper != null) {
                    md = lineMapper.apply(md);
                }
                mdContentBuilder.append(md);





            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return mdContentBuilder.toString();
    }

    public static final class PDFHelperBuilder {
        private Function<String, String> lineMapper;
        private String ocrLanguage;
        private String titleRegex;

        private PDFHelperBuilder() {
        }

        public static PDFHelperBuilder builder() {
            return new PDFHelperBuilder();
        }

        public PDFHelperBuilder withLineMapper(Function<String, String> lineMapper) {
            this.lineMapper = lineMapper;
            return this;
        }

        public PDFHelperBuilder withOcrLanguage(String ocrLanguage) {
            this.ocrLanguage = ocrLanguage;
            return this;
        }

        public PDFHelper build() {
            return new PDFHelper(lineMapper, ocrLanguage);
        }
    }
}
