package com.techatpark;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.Test;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


class TicketMakerTest {

    @Test
    void testSplit() throws IOException {

        Path path = Path.of("/home/haripriya/TNBOOKS/11th-Physics");

        // 1. Get PDF Files
        try (Stream<Path> walk = Files.walk(path )) {
            walk
                    .map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".pdf"))
                    .forEach(pdfFile -> {

                        // 2. Create Working folder for PDF
                        File pdfFolder = new File(pdfFile.getParentFile(), pdfFile.getName().replaceAll(".pdf","") );
                        try {
                            List<File> chapters = getChapters(pdfFolder, pdfFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }



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

                        String title = chapterPdfName;
                        info.setTitle(title);
                        info.setAuthor(chapterPdfName);

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

                    String title = chapterPdfName;
                    info.setTitle(title);
                    info.setAuthor(chapterPdfName);

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





}
