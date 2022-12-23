package com.techatpark;

import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BookCleaner {

    public void adjustTitles(String folderPath, String titleRegex, int grp) throws IOException {
        List<File> markdownFiles = findFiles(new File(folderPath), "md");

        markdownFiles.forEach(markdownFile -> {
            try {
                String title = getTitle(markdownFile,titleRegex,grp);

                System.out.println(title);

                if(!title.equals(markdownFile.getParentFile().getName())) {

                    String mdContent = Files.readString(markdownFile.toPath());

                    Files.writeString(markdownFile.toPath(), mdContent
                            .replaceFirst("title: '(.*?)'","title: '" + title + "'"));


                    File contentFolder = markdownFile.getParentFile();

                    while (!contentFolder.getName().startsWith("content.")) {
                        contentFolder = contentFolder.getParentFile();
                    }

                    if(contentFolder.getName().equals("content.en")) {
                        String newName = title.toLowerCase().replaceAll(" ", "-");

                        Pattern pattern = Pattern.compile("weight: ([0-9]+)");
                        Matcher matcher = pattern.matcher(mdContent);

                        if(matcher.find()) {
                            String weight = matcher.group(1);

                            List<File> conentFolders = Arrays.stream(contentFolder.getParentFile().listFiles((file, s) -> s.startsWith("content.")
                                    && !s.equals("content.en"))).toList();

                            conentFolders.forEach(o -> {
                                try {
                                    List<File> markdownFilesForLanguage = findFiles(o, "md");

                                    for (File markdownFileForLanguage:
                                         markdownFilesForLanguage) {
                                        if(markdownFileForLanguage.getParentFile().getParentFile().getName().equals(
                                                markdownFile.getParentFile().getParentFile().getName())
                                            &&
                                                Files.readString(markdownFileForLanguage.toPath()).contains("weight: " + weight)) {
                                            markdownFileForLanguage.getParentFile().renameTo(new File(markdownFileForLanguage.getParentFile().getParentFile(), newName));
                                        }
                                    }


                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }


                            });
                        }

                        markdownFile.getParentFile().renameTo(new File(markdownFile.getParentFile().getParentFile(), newName));
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    public String getTitle(File mdFile,String titleRegex, int grp) throws IOException {
        String title ;

        Pattern pattern = Pattern.compile(titleRegex);
        Matcher matcher = pattern.matcher(Files.readString(mdFile.toPath()));

        if(matcher.find()) {
            title = matcher.group(grp);
        }
        else {
            title = mdFile.getParentFile().getName();
        }
        return title;
    }

    public void removeDummyImages(String bookPath) throws IOException, InterruptedException {


            List<File> contentFoldersForLanguage = Arrays.stream(new File(bookPath).listFiles((file1, s) -> s.startsWith("content.") && !s.equals("content.en"))).toList();



            for (File contentFolderForLanguage:
            contentFoldersForLanguage) {
                List<File> imageFilesForLanuage = findFiles(contentFolderForLanguage, "jpg");

                for (File destImageFile:
                        imageFilesForLanuage) {
                    String imageContent = OCRHelper.getContent(destImageFile, "tam");

                    if(imageContent.trim().length() < 3) {
                        destImageFile.delete();
                    }

                }
            }

    }

    public boolean areTheseSameImages(File file1,File file2) {
        Image image1 = Toolkit.getDefaultToolkit().getImage(file1.getAbsolutePath());
        Image image2 = Toolkit.getDefaultToolkit().getImage(file2.getAbsolutePath());

        try {

            PixelGrabber grabImage1Pixels = new PixelGrabber(image1, 0, 0, -1,
                    -1, false);
            PixelGrabber grabImage2Pixels = new PixelGrabber(image2, 0, 0, -1,
                    -1, false);

            int[] image1Data = null;

            if (grabImage1Pixels.grabPixels()) {
                int width = grabImage1Pixels.getWidth();
                int height = grabImage1Pixels.getHeight();
                image1Data = new int[width * height];
                image1Data = (int[]) grabImage1Pixels.getPixels();
            }

            int[] image2Data = null;

            if (grabImage2Pixels.grabPixels()) {
                int width = grabImage2Pixels.getWidth();
                int height = grabImage2Pixels.getHeight();
                image2Data = new int[width * height];
                image2Data = (int[]) grabImage2Pixels.getPixels();
            }

            return java.util.Arrays.equals(image1Data, image2Data);

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public void removeUnavailableImages(String bookPath) {
        File file = new File(bookPath);

        if (file.exists()) {
            List<File> contentFolders = Arrays.stream(file.listFiles((file1, s) -> s.startsWith("content."))).toList();
            contentFolders.forEach(contentFolder -> {
                try {
                    List<File> markdownFiles = findFiles(contentFolder, "md");

                    markdownFiles.forEach(markdownFile -> {
                        try {
                            removeUnavailableImages(markdownFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } else {
            throw new RuntimeException("No Book available at " + bookPath);
        }
    }

    public void removeUnavailableImages(File markdownFile) throws IOException {

        StringBuilder content = new StringBuilder();

        Pattern pattern = Pattern.compile("!\\[\\]\\((.*jpg|png)");

        Files.readAllLines(markdownFile.toPath()).forEach(line -> {
            Matcher matcher = pattern.matcher(line);
            if(matcher.find()) {
                File imageFile = new File(markdownFile.getParentFile(),matcher.group(1)) ;
                if( imageFile.exists()) {
                    content.append(line).append('\n');
                } else {
                    System.out.println(imageFile);
                }
            } else {
                content.append(line).append('\n');
            }
        });

        Files.writeString(markdownFile.toPath(), content.toString());

    }

    private List<File> findFiles(File folder, String fileExtension)
            throws IOException {

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<File> result;

        try (Stream<Path> walk = Files.walk(folder.toPath())) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    // this is a folder, not string,
                    // this only test if folder end with a certain folder
                    //.filter(p -> p.endsWith(fileExtension))
                    // convert folder to string first
                    .map(p -> p.toFile())
                    .filter(f -> f.getName().endsWith(fileExtension))
                    .collect(Collectors.toList());
        }

        return result;
    }



}
