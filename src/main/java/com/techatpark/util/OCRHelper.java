package com.techatpark.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import io.github.furstenheim.CopyDown;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OCRHelper {

    public static String getContent(PDDocument document, String language) throws InterruptedException, IOException {

        StringBuilder stringBuilder = new StringBuilder();


        File tempFolder = Files.createTempDirectory(UUID.randomUUID().toString()).toFile();
        tempFolder.mkdirs();


            PDFRenderer pdfRenderer = new PDFRenderer(document);
        File imageFile;

            // int pageNumber = 0;

            // for (PDPage page : document.getPages()) {

        Parser parser = Parser.builder().build();

        HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        CopyDown copyDown = new CopyDown();

            for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); ++pageNumber) {
                BufferedImage bim = pdfRenderer.renderImage(pageNumber);

                BufferedImage bim1 = bim.getSubimage(0, 0, bim.getWidth()/2, bim.getHeight());
                imageFile = new File(tempFolder,"PageNo_" + pageNumber + "_1.png");
                ImageIO.write(bim1, "png", imageFile);

                stringBuilder.append(copyDown.convert(htmlRenderer.render(parser.parse(getContent(imageFile, language))))).append("\n");

                BufferedImage bim2 = bim.getSubimage(bim.getWidth()/2, 0, bim.getWidth()/2, bim.getHeight());
                imageFile = new File(tempFolder,"PageNo_" + pageNumber + "_2.png");
                ImageIO.write(bim2, "png", imageFile);

                stringBuilder.append(copyDown.convert(htmlRenderer.render(parser.parse(getContent(imageFile, language))))).append("\n");

            }

            document.close();




        return stringBuilder.toString();

    }

    public static String getContent(File imageFile, String language) throws InterruptedException, IOException {
        String textFileName = imageFile.getName().replaceAll("\\.","_");

        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(imageFile.getParentFile());

        if (isWindows) {
            builder.command("cmd.exe", "/c", "dir");
        } else {
            builder.command("sh", "-c", "tesseract -l " + language + " " + imageFile.getName() + " " + textFileName);
        }

        Process process = builder.start();

        if (!process.waitFor(100, TimeUnit.SECONDS)) {
            process.destroy();/*from   w  w  w .j a va  2s  . c  om*/
            throw new InterruptedException(
                    "Process has been interrupted because of timeout (" + 10 + "s). ");
        }


        BufferedReader stdin = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder result = new StringBuilder();
        String line;
        int linesCount = 0;

        while ((line = stdin.readLine()) != null) {
            linesCount++;
            result.append(line).append("\n");
        }

        if (linesCount == 0) {
            result.append("ERROR");

            while ((line = stderr.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        File textFile = new File(imageFile.getParentFile(),textFileName + ".txt");
        String content = Files.readString(textFile.toPath());
        textFile.delete();

        return content;
    }
}
