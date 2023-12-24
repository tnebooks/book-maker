package com.techatpark;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

class TNBookReaderTest {

    @Test
    void testExtractBook() throws IOException, InterruptedException {

        File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());
        tempDir.mkdirs();

        new TNBookReader("English",
                "/home/haripriya/TNBOOKS/10th_Social-Science-EM - www.tntextbooks.in.pdf",
                new Rectangle(10, 120, 300, 70)
                )
//                .transformFn(this::transformUsingRegEx)
//                .addLanguage("ta" ,
//                        "நுண்ணுயிரியல்",
//                        "/Users/thirumuruganmani/Downloads/11th_Micro_Biology_TM - www.tntextbooks.in.pdf")
//                .extract("/home/haripriya/Official/12th-english");
                .extract(tempDir.getAbsolutePath());

    }

    private String transformUsingRegEx(String input) {
        String replace = new String(input);
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\. (.*?)\\*\\*", "## $3\n");
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+) (.*?)\\*\\*", "## $3\n");


        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\.([0-9]+)\\. (.*?)\\*\\*", "### $4\n");
        replace = replace.replaceAll("\\*\\*([0-9]+)\\.([0-9]+)\\.([0-9]+) (.*?)\\*\\*", "### $4\n");

        return replace;
    }





    @Test
    void getChapterContent() throws IOException {
        TNBookReader tnBookReader = new TNBookReader("Botany",
                "/home/haripriya/TNBOOKS/12th_Botany_EM - www.tntextbooks.in.pdf",
                new Rectangle(10, 100, 320, 150));


        File pdf = new File("temp/botany/content.en/chapter-2.pdf");
        File md = new File("/home/haripriya/Official/11th-botany/content.en/botany/living-world/_index.md");
        StringBuilder stringBuilder = new StringBuilder("---\n" +
                "title: 'living-world'\n" +
                "weight: 2\n" +
                "---\n");

        stringBuilder.append(tnBookReader.getMarkdown(pdf));
        Files.writeString(md.toPath(),stringBuilder.toString());
    }

























}