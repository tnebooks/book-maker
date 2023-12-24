package com.techatpark;

import org.apache.pdfbox.tools.ExtractImages;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class BookEditHelperTest {

    private static String BOOK_PATH = "/home/haripriya/Official/relational-databases";

    @Test
    void extractImages() {
        String[] args = new String[]{"--input=/home/haripriya/TNBOOKS/11th_Botany_EM - www.tntextbooks.in.pdf", "-prefix=png"};
        ExtractImages.main(args);
    }

    @Test
    void fixImages() throws IOException {
        File bookFolder = new File(BOOK_PATH);

        File contentEnFolder = new File(bookFolder, "content.en");

        File[] files = recursiveListFiles(contentEnFolder, (file, name) -> name.equals("index.md"));

        for (File file : files) {

            List<String> lines = Files.readAllLines(file.toPath());

            for (String line : lines) {
                if (line.startsWith("![") && line.trim().endsWith(")")) {

                    String imageName = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));

                    new File(file.getParentFile().getParentFile(), imageName).renameTo(new File(file.getParentFile(), imageName));

                }
            }

        }
    }

    @Test
    void splitByHeading() {
        File bookFolder = new File(BOOK_PATH);

        File contentEnFolder = new File(bookFolder, "content.en");

        File[] folders = contentEnFolder.listFiles(file -> file.isDirectory());

        for (File folder : folders) {

            File file = new File(folder, "_index.md");

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                String chatpterTitle ;
                File chatpterFile = null;
                int weight = 1;
                StringBuilder contentOfChapter = new StringBuilder();

                for (int i = 1; i < lines.size(); i++) {

                    if(lines.get(i).startsWith("## ")) {
                        chatpterTitle = lines.get(i).replace("## ","");

                        chatpterFile = new File(file.getParentFile(), chatpterTitle.trim().toLowerCase()
                                .replaceAll(" ","-") + File.separator + "index.md");

                        if(!chatpterFile.exists()) {
                            chatpterFile.getParentFile().mkdirs();
                            contentOfChapter.append("---")
                                    .append("\ntitle: ")
                                    .append(chatpterTitle)
                                    .append("\nweight: ")
                                    .append(weight++)
                                    .append("\n---\n\n")
                                    .append(chatpterTitle);
                            Files.writeString(chatpterFile.toPath(), contentOfChapter, StandardOpenOption.CREATE_NEW);

                            contentOfChapter.setLength(0);
                        }



                    }

                    if(chatpterFile != null) {
                        Files.write(chatpterFile.toPath(), (lines.get(i) + "\n").getBytes(), StandardOpenOption.APPEND);
                    }
                }


                String content = new String(Files.readAllBytes(file.toPath()));
                file.delete();
                Files.writeString(file.toPath(), content.substring(0,content.indexOf("## ")), StandardOpenOption.CREATE_NEW);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }



    @Test
    void adjustAsPerEn() {

        File bookFolder = new File(BOOK_PATH);

        File contentEnFolder = new File(bookFolder, "content.en");

        File contentTaFolder = new File(bookFolder, "content.ta");

        File[] files = recursiveListFiles(contentEnFolder, (file, name) -> name.equals("index.md") || name.equals("_index.md"));

        for (File file :
                files) {
            File companion = new File(contentTaFolder,file.getAbsolutePath().split("content.en"+File.separator)[1]);
            if( !companion.exists()) {


                companion.getParentFile().mkdirs();
                try {
//                    companion.createNewFile();

                    List<String> lines = Files.readAllLines(file.toPath());
                    StringBuilder stringBuilder = new StringBuilder(lines.get(0)).append("\n");

                    for (int i = 1; i < lines.size(); i++) {
                        stringBuilder.append(lines.get(i)).append("\n");
                        if(lines.get(i).equals("---")) {
                            break;
                        }
                    }

                    Files.writeString(companion.toPath(), stringBuilder, StandardOpenOption.CREATE_NEW);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public static File[] recursiveListFiles(File dir, FilenameFilter filter) {
        if (!dir.isDirectory())
            throw new IllegalArgumentException(dir + " is not a directory");
        List<File> fileList = new ArrayList<File>();
        recursiveListFilesHelper(dir, filter, fileList);
        return fileList.toArray(new File[fileList.size()]);
    }//from w  w  w  . j a va  2 s.c om

    public static void recursiveListFilesHelper(File dir, FilenameFilter filter, List<File> fileList) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                recursiveListFilesHelper(f, filter, fileList);
            } else {
                if (filter.accept(f,f.getName()))
                    fileList.add(f);
            }
        }
    }
}
