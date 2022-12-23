package com.techatpark;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class TNBookReaderTest {

    @Test
    void getChapters() throws IOException, InterruptedException {


        new TNBookReader("Botany",
                "/home/haripriya/TNBOOKS/11th_Botany_EM - www.tntextbooks.in.pdf")
                .addLanguage("ta" ,
                        "தாவரவியல்",
                        "/home/haripriya/TNBOOKS/11th_Botany_TM - www.tntextbooks.in.pdf")
                .transformFn(this::getBotanyTransform)
                .extract("/home/haripriya/Official/11th-botany");


//        new TNBookReader("Maths",
//                "/home/haripriya/TNBOOKS/12th_Mathematics_Vol-1_EM - www.tntextbooks.in.pdf")
//                .addLanguage("ta" ,
//                        "கணிதம்",
//                        "/home/haripriya/TNBOOKS/12th_Mathematics_Vol-1_TM - www.tntextbooks.in.pdf")
//                .extract("/home/haripriya/Official/12th-maths");

    }

    @Test
    void getMathsContent() throws IOException {
        TNBookReader tnBookReader = new TNBookReader("Botany",
                "/home/haripriya/TNBOOKS/12th_Botany_EM - www.tntextbooks.in.pdf");
        File pdf = new File("temp/botany/content.en/chapter-10.pdf");
    }






    @Test
    void getChapterContent() throws IOException {
        TNBookReader tnBookReader = new TNBookReader("Botany",
                "/home/haripriya/TNBOOKS/12th_Botany_EM - www.tntextbooks.in.pdf")
                .transformFn(this::getBotanyTransform);


        File pdf = new File("temp/botany/content.en/chapter-2.pdf");
        File md = new File("/home/haripriya/Official/11th-botany/content.en/botany/living-world/_index.md");
        StringBuilder stringBuilder = new StringBuilder("---\n" +
                "title: 'living-world'\n" +
                "weight: 2\n" +
                "---\n");

        stringBuilder.append(tnBookReader.getMarkdown(pdf));
        Files.writeString(md.toPath(),stringBuilder.toString());
    }































    private String getZoologyTransform(String input) {
        String replace = new String(input);
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\. (.*?)\\*\\*", "## $3\n");
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+) (.*?)\\*\\*", "## $3\n");


        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\.([0-9]+)\\. (.*?)\\*\\*", "### $4\n");
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\.([0-9]+) (.*?)\\*\\*", "### $4\n");


        replace = replace.replaceAll("\\*\\*Figure ([0-9]+)\\.([0-9]+)\\*\\*(.*)","![$3]($1.$2.png \"\")\n");

        replace = replace.replaceAll("\\*\\*Summary\\*\\*", "**Summary**\n");
        replace = replace.replaceAll("➢ ","- ");

        replace = replace.replaceAll("The learner will be able to,","");
        return replace;
    }

    private String getBotanyTransform(String input) {
        String replace = new String(input);
        replace = replace.replaceAll("\\*\\*g\\*\\*","");
        replace = replace.replaceAll("\\*\\*otany\\*\\*","");

        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+) (.*?)\\*\\*", "## $3\n");

        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\.([0-9]+)\\. (.*?)\\*\\*", "### $4\n");
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\.([0-9]+) (.*?)\\*\\*", "### $4\n");

        replace = replace.replaceAll("\\*\\*Figure ([0-9]+)\\.([0-9]+):\\*\\*(.*)","![$3]($1.$2.png \"\")\n");
        replace = replace.replaceAll("\\*\\*Figure ([0-9]+)\\.([0-9]+) (.*)\\*\\*","![$3]($1.$2.png \"\")\n");

        replace = replace.replaceAll("\\*\\*Figure ([0-9]+)\\.([0-9]+)\\*\\*","![No Desc]($1.$2.png \"\")\n");

        replace = replace.replaceAll(". • ",". \n• ");

        replace = replace.replaceAll("\\*\\*ICT Corner\\*\\*(.*)","");

        replace = replace.replaceAll("• ","- ");
        return replace;
    }

    @Test
    void readPlain() throws IOException {
        File pdf = new File("temp/zoology/content.ta/chapter-2.pdf");

        try (PDDocument pdfDocument = Loader.loadPDF(pdf)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();

            PDDocument pageDocument = new PDDocument();

            pageDocument.addPage(pdfDocument.getPage(6));

            String content = pdfTextStripper.getText(pageDocument);
            //System.out.println(content);

            byte[] b = content.getBytes();
            String text = new String(b, "UTF-8");
            System.out.println(text);


        }




    }



//    PdfDocument pdf = new PdfDocument("temp/botany/content.en/chapter-4.pdf");
//
//    //Create a StringBuilder instance
//    StringBuilder builder = new StringBuilder();
//    //Create a PdfTableExtractor instance
//    PdfTableExtractor extractor = new PdfTableExtractor(pdf);
//
//    //Loop through the pages in the PDF
//        for (int pageIndex = 0; pageIndex < pdf.getPages().getCount(); pageIndex++) {
//        //Extract tables from the current page into a PdfTable array
//        PdfTable[] tableLists = extractor.extractTable(pageIndex);
//
//        //If any tables are found
//        if (tableLists != null && tableLists.length > 0) {
//            //Loop through the tables in the array
//            for (PdfTable table : tableLists) {
//                StringBuilder heading = new StringBuilder();
//                heading.append("|");
//                for (int j = 0; j < table.getColumnCount(); j++) {
//                    heading.append("------|");
//                }
//                heading.append("\n");
//                //Loop through the rows in the current table
//                for (int i = 0; i < table.getRowCount(); i++) {
//                    if(i == 1) {
//                        builder.append(heading);
//                    }
//
//                    StringBuilder rowText = new StringBuilder();
//
//                    //Loop through the columns in the current table
//                    for (int j = 0; j < table.getColumnCount(); j++) {
//                        //Extract data from the current table cell and append to the StringBuilder
//                        String text = table.getText(i, j).trim().replaceAll("\n","");
//                        if(text.length() != 0) {
//                            rowText.append(text + " |");
//                        }
//                    }
//
//                    if(rowText.toString().trim().length() != 0) {
//                        builder.append("| ").append(rowText);
//                    }
//
//
//                    builder.append("\r\n");
//                }
//            }
//
//            builder.append("\n\n\n\n");
//        }
//    }
//
//        System.out.println(builder);

}