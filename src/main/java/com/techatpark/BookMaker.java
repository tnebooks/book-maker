package com.techatpark;

import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BookMaker {

    private final PDFHelper pdfImporter;

    public BookMaker(final PDFHelper pdfImporter) {
        this.pdfImporter = pdfImporter;
    }


    public void extractPDF(File pdfFile, File destFolder) throws IOException {

        if(destFolder.exists()) {
            Files.walk(destFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }


        destFolder.mkdirs();

        List<File> pdfChapterFiles = null;

        if(!pdfChapterFiles.isEmpty()) {
            List<String> fileNames = pdfChapterFiles.stream().map(file -> file.getName()).collect(Collectors.toList());
            String commonPrefix = getCommonPrefix(fileNames);
            String commonSufix = getCommonSuffix(fileNames);
            AtomicInteger atomicInteger = new AtomicInteger(1);
            pdfChapterFiles.forEach(file -> {
                File newFile = new File(file.getParentFile(), getCleanFileName(file, commonPrefix, commonSufix));
                file.renameTo(newFile);
                if (newFile.exists()) {
                    generateMarkdown(destFolder, newFile,atomicInteger.getAndIncrement(), pdfImporter);
                }
            });
        }
    }

    private void generateMarkdown(File folder, File p, int weight, PDFHelper pdfImporter) {
        try {
            File markDownFile = new File(folder,p.getName().toLowerCase()
                    .replace(" ","-")
                    .replace(".pdf","") + File.separator + "_index.md");
            markDownFile.getParentFile().mkdirs();
            StringBuilder mdC = new StringBuilder(pdfImporter.getFrontMatter(p, weight));
            mdC.append(pdfImporter.getMarkdown(markDownFile, p));
            Files.writeString(markDownFile.toPath(), mdC.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCleanFileName(File file, String commonPrefix, String commonSufix) {
        String fileName = file.getName();
        return fileName.replaceFirst(commonPrefix,"")
                .replace(fileName.substring(fileName.lastIndexOf(commonSufix), fileName.length()), ".pdf");
    }

    private static String getCommonPrefix(final List<String> strings) {
        int result = 0;

        if (strings == null || strings.size() < 2) {
            return null;
        }

        for (int i = 0; i < strings.size() - 1; i++) {
            String prefix = Strings.commonPrefix(strings.get(i), strings.get(i + 1));
            result = result == 0 ?
                    prefix.length() :
                    Math.min(prefix.length(), result);
        }

        return result == 0 ? null : strings.get(0).substring(0,result);
    }

    private static String getCommonSuffix(final List<String> strings) {
        int result = 0;

        if (strings == null || strings.size() < 2) {
            return null;
        }

        for (int i = 0; i < strings.size() - 1; i++) {
            String suffix = Strings.commonSuffix(strings.get(i), strings.get(i + 1));
            result = result == 0 ?
                    suffix.length() :
                    Math.min(suffix.length(), result);
        }

        return result == 0 ? null : strings.get(0).substring(strings.get(0).length() - result);
    }

}
