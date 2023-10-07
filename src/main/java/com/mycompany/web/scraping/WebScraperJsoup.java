/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.web.scraping;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author akkus
 */
public class WebScraperJsoup {

    public void scrapeAllWithJsoup() {

        String csvFile = "main.csv"; // Bu kısım giriş CSV dosyanızın adına göre değiştirilmelidir.
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Başlık satırını atla

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                String title = data[0];
                String link = data[1];

                scrapeWithJsoup(title, link); // her satır için scrape fonksiyonunu çağır
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void scrapeWithJsoup(String title, String link) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Başlık satırını oluşturma
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("HREF");
        headerRow.createCell(1).setCellValue("SRC");
        headerRow.createCell(2).setCellValue("TTL");
        headerRow.createCell(3).setCellValue("NAME");
        headerRow.createCell(4).setCellValue("DISCOUNTED_PRICE");
        headerRow.createCell(5).setCellValue("ORIGINAL_PRICE");

        int rowNum = 1;

        for (int i = 1; i < 2; i++) {
            try {
                Document doc = Jsoup.connect(link + "?pi=" + i).get();
                System.out.println("*************************");
                System.out.println(link + "?pi=" + i);

                // Öğeleri seç
                Elements elements = doc.select(".p-card-chldrn-cntnr");
                for (Element el : elements) {
                    String href = el.select("a").first().attr("abs:href");  // 'abs:' kullanarak tam URL'yi alın.
                    String src = el.select(".p-card-img").attr("src");
                    String ttl = el.select(".prdct-desc-cntnr-ttl").text();
                    String name = el.select(".prdct-desc-cntnr-name").text();
                    String discountedPrice = el.select(".prc-box-dscntd").text();
                    String originalPrice = el.select(".prc-box-orgnl").text();

                    // Excel satırına yaz
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(href);
                    row.createCell(1).setCellValue(src);
                    row.createCell(2).setCellValue(ttl);
                    row.createCell(3).setCellValue(name);
                    row.createCell(4).setCellValue(discountedPrice);
                    row.createCell(5).setCellValue(originalPrice);
                }

            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.out.println(":HATA - 404 Hatası. URL bulunamadı: " + link + "?pi=" + i);
                    break;
                } else {
                    System.out.println(":HATA - HTTP Hatası. Kod: " + e.getStatusCode() + " URL: " + link);
                }
            } catch (IOException ex) {
                Logger.getLogger(WebScraperJsoup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Excel dosyasını kaydet
        try (FileOutputStream fileOut = new FileOutputStream("cikti/" + title + ".xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
