package com.techatpark;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class BookMakerTest {

    @Test
    void extractBotonyForEnglish() throws IOException {

        PDFHelper pdfImporter = PDFHelper.PDFHelperBuilder.builder().withLineMapper(s ->
                s.replaceAll("www.tntextbooks.in", "")
                        .replaceAll("(.*) \\d\\d-\\d\\d-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d", "")
                        .replaceAll("\\*\\*([0-9]+)\\*\\*(.*)", "")
                        .replaceAll("\\n\\*\\*([0-9]+.[0-9]+.[0-9]+) (.*?)\\*\\*","\n### $2\n")
                        .replaceAll("\\n\\*\\*([0-9]+.[0-9]+) (.*?)\\*\\*","\n## $2\n")
                        .replaceAll("\\*\\*Chapter outline\\*\\*((.|\\n)*)\\*\\*Learning Objectives\\*\\*", "\n**Learning Objectives**\n")

        ).build();

        BookMaker bookMaker = new BookMaker(pdfImporter);
        
        bookMaker.extractPDF(
                new File("/home/haripriya/TNBOOKS/12th_Botany_EM - www.tntextbooks.in.pdf"),
                new File("/home/haripriya/Official/12th-biology/content.en/botany"));



    }

    @Test
    void extractZoologyForEnglish() throws IOException {

        PDFHelper pdfImporter = PDFHelper.PDFHelperBuilder.builder().withLineMapper(s ->
                s.replaceAll("www.tntextbooks.in", "")
                        .replaceAll("(.*) \\d\\d-\\d\\d-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d", "")
                        .replaceAll("\\*\\*([0-9]+)\\*\\*(.*)", "")
        ).build();

        BookMaker bookMaker = new BookMaker(pdfImporter);
        bookMaker.extractPDF(
                new File("/home/haripriya/TNBOOKS/12th_Zoology_EM - www.tntextbooks.in.pdf"),
                new File("/home/haripriya/Official/12th-biology/content.en/zoology"));


    }

    @Test
    void extractZoologyTamil() throws IOException {

        PDFHelper pdfImporter = PDFHelper.PDFHelperBuilder.builder().withLineMapper(s ->
                s.replaceAll("www.tntextbooks.in", "")
        ).withOcrLanguage("tam").build();

        BookMaker bookMaker = new BookMaker(pdfImporter);

        bookMaker.extractPDF(
                new File("/home/haripriya/TNBOOKS/12th_Zoology_TM - www.tntextbooks.in.pdf"),
                new File("/home/haripriya/Official/12th-biology/content.ta/zoology"));

    }


}