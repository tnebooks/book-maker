package com.techatpark;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class BookCleanerTest {

    @Test
    void cleanupImages() throws IOException, InterruptedException {
        new BookCleaner().removeUnavailableImages("/home/haripriya/Official/12th-biology");

        // new BookCleaner().removeDummyImages("/home/haripriya/Official/12th-biology");


    }

    @Test
    void cleanup() throws IOException {
        new BookCleaner()
                .adjustTitles("/home/haripriya/Official/12th-biology/content.en/botany",
                        "\\*\\*([0-9]+) ([A-Z][a-z].*)\\*\\*",
                        2);

        new BookCleaner()
                .adjustTitles("/home/haripriya/Official/12th-biology/content.en/zoology",
                        "\\n\\*\\*([A-Z][a-z].*)\\*\\*",
                        1);

    }


}