package com.edu.escuelaing.arep.serverConcurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlFetcher {

    public static byte[] readFile(String filePath) throws IOException {
        String basePath = "src/main/resources/public/";

        if (filePath.isEmpty()) {
            filePath = "public/index.html";

        }
        return Files.readAllBytes(Paths.get(basePath + filePath));
    }

    public static String getMimeType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
