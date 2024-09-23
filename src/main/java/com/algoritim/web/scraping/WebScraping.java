/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.algoritim.web.scraping;

import static com.algoritim.web.scraping.UpdateFiles.updateExcel;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author akkus
 */
public class WebScraping {

    public static void main(String[] args) {
        new WebScraper().scrapeAll();
        //new WebScraperJsoup().scrapeAllWithJsoup();

        Path dir = Paths.get("cikti"); // XLSX dosyalarının bulunduğu dizin
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.xlsx")) {
            for (Path entry : stream) {
                updateExcel(entry.toFile());
            }
        } catch (IOException ex) {
            Logger.getLogger(WebScraping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
