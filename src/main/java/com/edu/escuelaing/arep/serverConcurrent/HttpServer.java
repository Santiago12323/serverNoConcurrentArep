package com.edu.escuelaing.arep.serverConcurrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        int port = 35000;

        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Servidor iniciado en puerto " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }
    }


    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            System.out.println("Petición recibida: " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String filePath = parts[1];
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            try {
                byte[] fileContent = HtmlFetcher.readFile(filePath);
                String contentType = HtmlFetcher.getMimeType(filePath);

                writer.print("HTTP/1.1 200 OK\r\n");
                writer.print("Content-Type: " + contentType + "\r\n");
                writer.print("Content-Length: " + fileContent.length + "\r\n");
                writer.print("Access-Control-Allow-Origin: *\r\n");
                writer.print("\r\n");
                writer.flush();

                clientSocket.getOutputStream().write(fileContent);
                clientSocket.getOutputStream().flush();

            } catch (IOException e) {
                writer.print("HTTP/1.1 404 Not Found\r\n");
                writer.print("Content-Type: text/plain\r\n");
                writer.print("\r\n");
                writer.print("Archivo no encontrado: " + filePath);
                writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Error procesando la solicitud: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error cerrando el socket: " + e.getMessage());
            }
        }
    }



}
