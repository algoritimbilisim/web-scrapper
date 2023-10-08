/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.algoritim.web.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class UpdateFiles {

    public static void updateCSV(Path csvFile) throws IOException {
        List<String> lines = Files.readAllLines(csvFile);
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split(",");
            if (data.length > 1) { // En azından href kolonu olduğundan emin olun
                String href = data[0];
                
                Document doc = Jsoup.connect(href).get();
                String detailName = fetchDetailName(doc);
                // İlgili satırın 9. sütununa detailName ekleyin
                if (data.length >= 9) {
                    data[8] = detailName;
                } else {
                    String[] newData = new String[9];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    newData[8] = detailName;
                    data = newData;
                }
                lines.set(i, String.join(",", data));
            }
        }
        // Değişiklikleri dosyaya geri yaz
        Files.write(csvFile, lines);
    }

    public static void updateExcel(File xlsxFile) throws IOException {
        FileInputStream fis = new FileInputStream(xlsxFile);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        int lastRowNum = sheet.getLastRowNum();

        for (int i = 1; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getCell(0) != null) {
                String href = row.getCell(0).getStringCellValue();
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println(href);
                Document doc = Jsoup.connect(href).get();
                String detailName = fetchDetailName(doc);
                String imageSources = fetchImageSources(doc);

                // İlgili satırın 9. hücresine detailName ekleyin
                Cell cellDetail = row.createCell(8, CellType.STRING);
                cellDetail.setCellValue(detailName);

                // İlgili satırın 10. hücresine imageSources ekleyin
                Cell cellImages = row.createCell(9, CellType.STRING);
                cellImages.setCellValue(imageSources);
            }
        }

        // Değişiklikleri dosyaya geri yaz
        FileOutputStream fos = new FileOutputStream(xlsxFile);
        workbook.write(fos);
        workbook.close();
        fos.close();
        fis.close();
    }

    public static String fetchDetailName(Document doc) {

        return doc.select(".detail-name").text();

    }

    public static String fetchImageSources(Document doc) {
        StringBuilder sb = new StringBuilder();
        Elements images = doc.select(".product-slide img");

        for (Element img : images) {
            String src = img.attr("src");
            src = src.replace("https://cdn.dsmcdn.com/mnresize/128/192/", "https://cdn.dsmcdn.com/");
            sb.append(src).append(","); // Eğer birden fazla resim varsa aralarına boşluk koyarak ekleyin
        }

        return sb.toString().trim();

    }

}
