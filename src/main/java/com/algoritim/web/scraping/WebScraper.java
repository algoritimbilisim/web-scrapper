/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.algoritim.web.scraping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebScraper {

    WebDriver driver;

    public void scrapeAll() {

        // Sürücü yolunu belirtin
        System.setProperty("webdriver.chrome.driver", "./chromedriver");

        // WebDriver nesnesini oluştur
        driver = new ChromeDriver();
        //ChromeOptions options = new ChromeOptions();
        //options.setExperimentalOption("useAutomationExtension", false);
        //options.addArguments("--remote-debugging-port=9225");
        //driver = new ChromeDriver(options);

        String csvFile = "main.csv"; // Bu kısım giriş CSV dosyanızın adına göre değiştirilmelidir.
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Başlık satırını atla

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                String title = data[0];
                String link = data[1];

                scrape(title, link); // her satır için scrape fonksiyonunu çağır
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Tarayıcıyı kapat
        driver.quit();
    }

    public void scrape(String title, String link) {
        long previousHeight = 0, currentHeight = 0;
        // URL'yi ziyaret et
        driver.get(link); // Ya da istediğiniz URL
        // Scroll için JavascriptExecutor'u tanımla
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 10 kez aşağıya scroll yap
        for (int i = 0; i < 2; i++) {
            previousHeight = (long) js.executeScript("return document.body.scrollHeight;");

            js.executeScript("window.scrollBy(0,2000)");

            try {
                Thread.sleep(1000);  // Scroll sonrası beklemek için
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            currentHeight = (long) js.executeScript("return document.body.scrollHeight;");

            // Eğer yükseklik değişmediyse, sayfanın sonuna ulaşmışız demektir.
            if (previousHeight == currentHeight) {
                break;
            }
        }

        // Öğeleri bul
        List<WebElement> elements = driver.findElements(By.className("p-card-chldrn-cntnr"));

        // CSV dosyasını yazma
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("cikti/" + title + ".csv"))) {
            // Başlık satırını yaz
            bw.write("HREF,SRC,TTL,NAME,DISCOUNTED_PRICE,ORIGINAL_PRICE");
            bw.newLine();

            for (WebElement el : elements) {
                String href = el.findElement(By.tagName("a")).getAttribute("href");
                String src = el.findElement(By.className("p-card-img")).getAttribute("src");
                String ttl = el.findElement(By.className("prdct-desc-cntnr-ttl")).getText();
                String name = el.findElement(By.className("prdct-desc-cntnr-name")).getText();
                String discountedPrice = "";
                String originalPrice = "";
                try {
                    discountedPrice = el.findElement(By.className("prc-box-dscntd")).getText();
                    originalPrice = el.findElement(By.className("prc-box-orgnl")).getText();
                } catch (Exception e) { // Eğer bu sınıf adına sahip bir öğe yoksa hatayı yakala
                    // Eksik fiyat bilgisi durumu için bir şey yapmıyoruz, sadece hata mesajını önlemek için
                }

                // CSV formatında satırı yaz
                bw.write(href + "," + src + "," + ttl + "," + name + "," + discountedPrice + "," + originalPrice);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ImageDownloader().DownloadImages(title);

    }
}
