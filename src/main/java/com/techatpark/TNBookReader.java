package com.techatpark;

import com.techatpark.util.OCRHelper;
import com.techatpark.util.RegionBasedTextStripper;

import io.github.furstenheim.CopyDown;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TNBookReader {

    private final String bookName;

    private final String bookPdf;

    private final List<Language> languages;

    private final Rectangle rectLeft;
    private final Rectangle rectRight;

    private final Rectangle titleRectangle;

    private Function<String, String> transformFn;

    public TNBookReader(final String bookName, final String bookPdf,
                        final Rectangle titleRectangle) {
        this.bookName = bookName;
        this.bookPdf = bookPdf;
        this.languages = new ArrayList<>();
        this.titleRectangle = titleRectangle;

        rectLeft = new Rectangle(10, 50, 320, 750);
        rectRight = new Rectangle(330, 50, 320, 750);

    }

    public TNBookReader transformFn(Function<String, String> transformFn) {
        this.transformFn = transformFn;
        return this;
    }



    public TNBookReader addLanguage(String code, final String bookName, final String bookPdf) {
        this.languages.add(new Language(code, bookName, bookPdf));
        return this;
    }


    public void extract(String bookPath) throws IOException, InterruptedException {

        File bookRoot = new File(bookPath);

        bookRoot.mkdirs();

        File tempFolder = new File("temp" + File.separator + bookName.toLowerCase());
// pdf split to chapter wise seperated files

        java.util.List<File> chapterFiles = getChapters(new File(tempFolder, "content.en"), new File(bookPdf));

        this.languages.forEach(language -> {
            try {
                getChapters(new File(tempFolder, "content." + language.code), new File(language.bookPdf));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // creating folders language specific
        File englishContentFolder = new File(bookRoot, "content.en" + File.separator + bookName.toLowerCase());

        englishContentFolder.mkdirs();

        for (int i = 0; i < chapterFiles.size(); i++) {
            int number = i;
            extractChapter(bookRoot, tempFolder, chapterFiles, englishContentFolder, number);
        }

    }

    private Void extractChapter(final File bookRoot, final File tempFolder, final List<File> chapterFiles, final File englishContentFolder, final int i) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(chapterFiles.get(i))) {
            PDDocumentInformation info = pdDocument.getDocumentInformation();
            String folderName = info.getAuthor();

            //creating a sub folder for each chapter
            File mdFile = new File(englishContentFolder, folderName + File.separator + "_index.md");
            mdFile.getParentFile().mkdirs();

            String frontMatter = "---\n" +
                    "title: '"+ info.getTitle() +"'\n" +
                    "weight: "+ (i +1) +"\n" +
                    "---\n\n" + getMarkdown(chapterFiles.get(i));

            Files.writeString(mdFile.toPath(), frontMatter);
//            language wise parsing pdf
            for (Language language:
            this.languages) {

                File languageContentFolder = new File(bookRoot, "content." + language.code + File.separator + bookName.toLowerCase());
                languageContentFolder.mkdirs();
                File languageMdFile = new File(languageContentFolder, folderName + File.separator + "_index.md");
                languageMdFile.getParentFile().mkdirs();

                File languagePdf = new File(tempFolder, "content." + language.code + File.separator + chapterFiles.get(i).getName());

                if(languagePdf.exists()) {
                    PDDocument pdDocumentForLanguage = Loader.loadPDF(languagePdf);

                    String markdown = getMarkdown(languagePdf);


                    String frontMatterForLanguage = "---\n" +
                            "title: '"+ pdDocumentForLanguage.getDocumentInformation().getTitle() +"'\n" +
                            "weight: "+ (i +1) +"\n" +
                            "---\n\n" +
                            markdown;
                    Files.writeString(languageMdFile.toPath(), frontMatterForLanguage);
                }



            }

        }
        return null;
    }


    private java.util.List<File> getChapters(File folder, File pdfFile) throws IOException {
        folder.mkdirs();

        List<File> children;

        try (PDDocument pdDocument = Loader.loadPDF(pdfFile)) {

            PDDocumentOutline outline = pdDocument.getDocumentCatalog().getDocumentOutline();

            if (outline != null) {
                PDPageTree pageTree = pdDocument.getPages();

                children = new ArrayList<>();

                int chapterNumber = 0;

                int startPg = 0;

                for (PDOutlineItem item : outline.children()) {
                    chapterNumber++;

                    String chapterPdfName = "chapter-" + chapterNumber;

                    File child = new File(folder, chapterPdfName + ".pdf");

                    PDPage currentPage = item.findDestinationPage(pdDocument);
                    startPg = pageTree.indexOf(currentPage);

                    if (item.getNextSibling() != null) {
                        PDPage nextIndexPage = item.getNextSibling().findDestinationPage(pdDocument);
                        int endPg = pageTree.indexOf(nextIndexPage);
                        PDDocument childDocument = new PDDocument();
                        for (int i = startPg; i < endPg; i++) {
                            childDocument.addPage(pageTree.get(i));
                        }


                        System.out.println(item.getTitle());




                        PDDocumentInformation info = childDocument.getDocumentInformation();

                        String title = getTitle(chapterPdfName, pageTree.get(startPg));
                        info.setTitle(title);
                        info.setAuthor(getSeoFolderName(title));

                        childDocument.save(child);
                        childDocument.close();

                    }

                    if(child.exists()) {
                        children.add(child);
                    }


                }

                if(startPg < pageTree.getCount()) {
                    PDDocument pdDocument1 = new PDDocument();

                    for (int i = startPg; i < pageTree.getCount(); i++) {
                        pdDocument1.addPage(pdDocument.getPage(i));
                    }

                    String chapterPdfName = "chapter-" + chapterNumber;

                    File child = new File(folder, chapterPdfName + ".pdf");

                    PDDocumentInformation info = pdDocument1.getDocumentInformation();

                    String title = getTitle(chapterPdfName, pageTree.get(startPg));
                    info.setTitle(title);
                    info.setAuthor(getSeoFolderName(title));

                    pdDocument1.save(child);
                    pdDocument1.close();
                    children.add(child);
                }


            } else {
                children = new ArrayList<>();
            }

        }

        return children;
    }

    private String getSeoFolderName(final String title) {
        String fName = title.toLowerCase()
                .replaceAll("\n", "-")
                .replaceAll(" ", "-")
                .replaceAll("---", "-")
                .replaceAll("--", "-")
                .replaceAll("[^a-z0-9-]", "");

        if(fName.length() > 75) {
            fName = fName.substring(0,75);
        }

        if(fName.startsWith("-")) {
            fName = fName.substring(1);
        }

        return fName;
    }

    private String getTitle(String chapterPdfName, final PDPage pageOne) throws IOException {
        PDFTextStripperByArea textStripper = new PDFTextStripperByArea();
        textStripper.addRegion("region", titleRectangle);
        textStripper.extractRegions(pageOne);

        String textForRegion = textStripper.getTextForRegion("region")
                .replaceAll("\\d", "")
                .replaceAll(": ","-")
                .replaceAll("\n"," ")
                .trim();

        int indexOf = textForRegion.toLowerCase().indexOf("chapter outline");
        if(indexOf != -1) {
            textForRegion = textForRegion.substring(0, indexOf);
        }

        //System.out.println(textForRegion);
        return textForRegion.length() == 0 ? chapterPdfName : textForRegion;
    }


    public String getMarkdown(File chapterPdfFile) throws IOException {
        StringBuilder pdfText = new StringBuilder();

        if(chapterPdfFile.getAbsolutePath().contains("content.ta")) {
            try {
                pdfText.append(OCRHelper.getContent(Loader.loadPDF(chapterPdfFile), "tam"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (PDDocument chapterPdfDocument = Loader.loadPDF(chapterPdfFile)) {
                int pageNumber = chapterPdfDocument.getNumberOfPages();
                for (int i = 0; i < pageNumber; i++) {
                    pdfText.append(getMarkdown(chapterPdfDocument.getPage(i)));
                }
            }
        }


        return transformFn == null ? pdfText.toString() : transformFn.apply(pdfText.toString());
    }

    private String getMarkdown(final PDPage pdPage) throws IOException {
        StringBuilder pageMarkdown = new StringBuilder();
        CopyDown copyDown = new CopyDown();

        RegionBasedTextStripper leftContentExtractor = new RegionBasedTextStripper(rectLeft);
        RegionBasedTextStripper rightContentExtractor = new RegionBasedTextStripper(rectRight);
        PDDocument document = new PDDocument();
        document.addPage(pdPage);

        pageMarkdown
                .append(copyDown.convert(leftContentExtractor.getText(document)))
                .append(copyDown.convert(rightContentExtractor.getText(document)));


        try {
            pageMarkdown
                    .append("\n")
                    .append(getTables(pdPage).stream().collect(Collectors.joining("\n\n")));
        }catch (Exception e) {
            System.out.println(e);
        }



        return pageMarkdown.toString();
    }

    private List<String> getTables(final PDPage pdPage)  {
        List<String> tables = new ArrayList<>();

        try {
            File tempFile = Files.createTempFile(String.valueOf(System.currentTimeMillis()), "pdf").toFile();
            PDDocument document = new PDDocument();
            document.addPage(pdPage);
            document.save(tempFile);

            PdfDocument pdf = new PdfDocument(tempFile.getAbsolutePath());
            //Create a PdfTableExtractor instance
            PdfTableExtractor extractor = new PdfTableExtractor(pdf);

            if(extractor != null) {
                //Loop through the pages in the PDF
                for (int pageIndex = 0; pageIndex < pdf.getPages().getCount(); pageIndex++) {


                    //Extract tables from the current page into a PdfTable array

                    PdfTable[] tableLists  = extractor.extractTable(pageIndex);
                    if (tableLists != null) {
                        //Loop through the tables in the array
                        for (PdfTable table : tableLists) {

                            StringBuilder builder = new StringBuilder();

                            StringBuilder heading = new StringBuilder();
                            heading.append("|");
                            for (int j = 0; j < table.getColumnCount(); j++) {
                                heading.append("------|");
                            }
                            heading.append("\n");
                            //Loop through the rows in the current table
                            for (int i = 0; i < table.getRowCount(); i++) {
                                if (i == 1) {
                                    builder.append(heading);
                                }

                                StringBuilder rowText = new StringBuilder();

                                //Loop through the columns in the current table
                                for (int j = 0; j < table.getColumnCount(); j++) {
                                    //Extract data from the current table cell and append to the StringBuilder
                                    String text = table.getText(i, j).trim().replaceAll("\n", "");
                                    if (text.length() != 0) {
                                        rowText.append(text + " |");
                                    }
                                }

                                if (rowText.toString().trim().length() != 0) {
                                    builder.append("| ").append(rowText);
                                }
                                builder.append("\r\n");
                            }

                            tables.add(builder.toString());
                        }
                    }

                    //If any tables are found

                }
            }
        }catch (Exception e) {

        }




        return tables;
    }


    private class Language {

        private final String code;
        private final String bookName;
        private final String bookPdf;

        Language(String code, final String bookName, final String bookPdf) {
            this.code = code;
            this.bookName = bookName;
            this.bookPdf = bookPdf;
        }
    }

}
