/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.algoritim.web.scraping;

/**
 *
 * @author akkus
 */
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ImageDownloader {

    public void DownloadImages(String title) {
        String csvFile = "cikti/" + title + ".csv";
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Başlık satırını atla
            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                String href = data[0];
                String src = data[1];
                String ttl = sanitizeString(data[2]);
                String name = sanitizeString(ttl + "_" + data[3]);

                // ttl dizinini kontrol et
                if (!Files.exists(Paths.get("cikti/" + ttl))) {
                    Files.createDirectories(Paths.get("cikti/" + ttl));
                }

                // Resmi indir
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet httpGet = new HttpGet(src);
                    httpClient.execute(httpGet, response -> {
                        String extension = getExtensionFromMimeType(response.getEntity().getContentType().getValue());
                        Files.copy(response.getEntity().getContent(), Paths.get("cikti/" + ttl, name + extension), StandardCopyOption.REPLACE_EXISTING);
                        return null;
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String sanitizeString(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "_").replace(" ", "_");
    }

    private static String getExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            default:
                return ""; // veya diğer türler için bir varsayılan uzantı sağlayın
        }
    }
}
