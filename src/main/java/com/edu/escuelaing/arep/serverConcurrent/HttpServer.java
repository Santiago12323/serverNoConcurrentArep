package com.edu.escuelaing.arep.serverConcurrent;

import com.edu.escuelaing.arep.serverConcurrent.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private static methodImpl methodService = new methodImpl();

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
            System.out.println(filePath);

            if (filePath.contains("/app")) {
                System.out.println("rest");
                serverRest(clientSocket, writer, parts[0], filePath, in);
            } else if (filePath.startsWith("/")) {
                System.out.println("disk");
                filePath = filePath.substring(1);
                findByDisk(clientSocket, writer, filePath);
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

    public static void findByDisk(Socket clientSocket, PrintWriter writer, String filePath) {
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
    }

    public static void serverRest(Socket clientSocket, PrintWriter writer, String method, String filePath, BufferedReader inReader) {
        try {
            String name = null;
            String json;
            int contentLength;

            switch (method) {
                case "GET":
                    if (filePath.contains("?name=")) {
                        name = filePath.split("\\?name=")[1];
                        User user = methodService.getImplement(name);

                        if (user != null) {
                            json = user.toString();
                        } else {
                            json = "{ \"error\": \"Usuario no encontrado\" }";
                        }
                    } else {
                        ObjectMapper mapperAll = new ObjectMapper();
                        json = mapperAll.writeValueAsString(methodService.getAll());
                    }

                    writer.print("HTTP/1.1 200 OK\r\n");
                    writer.print("Content-Type: application/json\r\n");
                    writer.print("Content-Length: " + json.getBytes().length + "\r\n");
                    writer.print("Access-Control-Allow-Origin: *\r\n");
                    writer.print("\r\n");
                    writer.flush();

                    clientSocket.getOutputStream().write(json.getBytes());
                    clientSocket.getOutputStream().flush();
                    break;


                case "POST":
                    contentLength = 0;
                    String headerLine;
                    while ((headerLine = inReader.readLine()) != null && !headerLine.isEmpty()) {
                        if (headerLine.startsWith("Content-Length:")) {
                            contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                        }
                    }

                    char[] bodyChars = new char[contentLength];
                    inReader.read(bodyChars, 0, contentLength);
                    String body = new String(bodyChars);

                    ObjectMapper mapper = new ObjectMapper();
                    User newUser = mapper.readValue(body, User.class);
                    methodService.postImplement(newUser);

                    String response = mapper.writeValueAsString(newUser);

                    writer.print("HTTP/1.1 200 OK\r\n");
                    writer.print("Content-Type: application/json\r\n");
                    writer.print("Content-Length: " + response.getBytes().length + "\r\n");
                    writer.print("Access-Control-Allow-Origin: *\r\n");
                    writer.print("\r\n");
                    writer.flush();

                    clientSocket.getOutputStream().write(response.getBytes());
                    clientSocket.getOutputStream().flush();
                    break;

                case "PUT":
                    if (filePath.contains("?name=")) {
                        name = filePath.split("\\?name=")[1];
                    }

                    if (name == null || name.isEmpty()) {
                        String notAllowed = "<html><body><h1>405 Method Not Allowed</h1><p>Debe enviar ?name= en la URL</p></body></html>";
                        writer.print("HTTP/1.1 405 Method Not Allowed\r\n");
                        writer.print("Content-Type: text/html\r\n");
                        writer.print("Content-Length: " + notAllowed.getBytes().length + "\r\n");
                        writer.print("Access-Control-Allow-Origin: *\r\n");
                        writer.print("\r\n");
                        writer.print(notAllowed);
                        writer.flush();
                        break;
                    }


                    contentLength = 0;
                    while ((headerLine = inReader.readLine()) != null && !headerLine.isEmpty()) {
                        if (headerLine.startsWith("Content-Length:")) {
                            contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                        }
                    }

                    char[] bodyCharsPut = new char[contentLength];
                    inReader.read(bodyCharsPut, 0, contentLength);
                    String bodyPut = new String(bodyCharsPut);

                    ObjectMapper mapperPut = new ObjectMapper();
                    User updateUser = mapperPut.readValue(bodyPut, User.class);

                    User updated = methodService.putImplement(updateUser);

                    String responsePut = mapperPut.writeValueAsString(updated);

                    writer.print("HTTP/1.1 200 OK\r\n");
                    writer.print("Content-Type: application/json\r\n");
                    writer.print("Content-Length: " + responsePut.getBytes().length + "\r\n");
                    writer.print("Access-Control-Allow-Origin: *\r\n");
                    writer.print("\r\n");
                    writer.flush();

                    clientSocket.getOutputStream().write(responsePut.getBytes());
                    clientSocket.getOutputStream().flush();
                    break;

                case "DELETE":
                    if (filePath.contains("?name=")) {
                        name = filePath.split("\\?name=")[1];
                    }

                    if (name == null || name.isEmpty()) {
                        String notAllowedDel = "<html><body><h1>405 Method Not Allowed</h1><p>Debe enviar ?name= en la URL</p></body></html>";
                        writer.print("HTTP/1.1 405 Method Not Allowed\r\n");
                        writer.print("Content-Type: text/html\r\n");
                        writer.print("Content-Length: " + notAllowedDel.getBytes().length + "\r\n");
                        writer.print("Access-Control-Allow-Origin: *\r\n");
                        writer.print("\r\n");
                        writer.print(notAllowedDel);
                        writer.flush();
                        break;
                    }

                    methodService.deleteImplement(name);
                    json = "{ \"message\": \"usuario eliminado\" }";

                    writer.print("HTTP/1.1 200 OK\r\n");
                    writer.print("Content-Type: application/json\r\n");
                    writer.print("Content-Length: " + json.getBytes().length + "\r\n");
                    writer.print("Access-Control-Allow-Origin: *\r\n");
                    writer.print("\r\n");
                    writer.flush();

                    clientSocket.getOutputStream().write(json.getBytes());
                    clientSocket.getOutputStream().flush();
                    break;

                default:
                    writer.print("HTTP/1.1 405 Method Not Allowed\r\n");
                    writer.print("Content-Type: text/plain\r\n");
                    writer.print("\r\n");
                    writer.print("Método no permitido: " + method);
                    writer.flush();
                    break;
            }
        } catch (IOException e) {
            writer.print("HTTP/1.1 500 Internal Server Error\r\n");
            writer.print("Content-Type: text/plain\r\n");
            writer.print("\r\n");
            writer.print("Error procesando la solicitud: " + e.getMessage());
            writer.flush();
        }
    }
}
