package com.techatpark;

import io.github.furstenheim.CopyDown;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SoupTest {

    @Test
    void tamilBooks() throws IOException {
        Document doc = Jsoup.connect("http://www.aramseyavirumbu.com").get();
        Elements newsHeadlines = doc.select("#content a");
        for (Element headline : newsHeadlines) {
            if (Integer.parseInt(headline.id()) > 72) {
                String chudi = headline.text().replaceAll("([0-9]+) ","")
                        .replace(".","");
                Files.writeString(
                        new File("/home/haripriya/Official/gurukulam/site/content.en/books/tamil-sangam/aticuti/uyir-varukkam/" + headline.id() + ".md").toPath(),
                        getContent(headline.id(),chudi));
            }
        }
    }

    private String getContent(String id,String title) throws IOException {
        StringBuilder stringBuilder = new StringBuilder("---\n" +
                "title: '"+title+"'\n" +
                "weight: "+id+"\n" +
                "---\n");
        Document doc = Jsoup.connect("http://www.aramseyavirumbu.com/wikis/"+id).get();
        Element newsHeadlines = doc.getElementById("main_container");
        CopyDown copyDown = new CopyDown();
        stringBuilder.append(copyDown.convert(newsHeadlines.html()));
        return stringBuilder.toString();
    }
}
