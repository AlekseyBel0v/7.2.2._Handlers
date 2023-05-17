

//ТЗ по ссылке - https://github.com/netology-code/jspr-homeworks/tree/master/02_forms

package ru.netology;

import org.apache.hc.core5.http.NameValuePair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    static int port = 9999;
    static int threadQuantity = 64;
    //static int memoryForRequestReading = 1024;

    public static void main(String[] args) {
        //Создание файла конфигураций с номером порта и количеством потоков, которые обрабатывают запросы на сервере
        final var configs = Path.of("src/main/resources/configs.csv");
        if (!Files.exists(configs)) {
            try {
                Files.createFile(configs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (final var bos = Files.newBufferedWriter(configs)) {
                bos.write("port:" + port + "\n" +
                          "thread quantity:" + threadQuantity + "\n");
                //"memory for request reading:" + memoryForRequestReading);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (final var bis = Files.newBufferedReader(configs)) {
            port = Integer.parseInt(bis.readLine().split(":")[1]);
            threadQuantity = Integer.parseInt(bis.readLine().split(":")[1]);
            //memoryForRequestReading = Integer.parseInt(bis.readLine().split(":")[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var server = new Server(port, threadQuantity);

        server.addHandler("/message", Method.GET, new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                var path = Path.of("public", request.getPath() + ".html");
                Main.sendFile(request, responseStream, path);
                var queryParams = request.getQueryParams();
                if (queryParams != null) {
                    System.out.println("Hello from get!\n");
                    System.out.println("Параметры из query string: \n");
                    for (NameValuePair pair : queryParams) {
                        System.out.println(pair.getName() + " = " + pair.getValue() + "\n");
                    }
                }
            }
        });

        server.addHandler("/message", Method.POST, new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                var path = Path.of("public", request.getPath() + ".html");
                sendFile(request, responseStream, path);
                System.out.println("Hello from POST!\n");
                var params = request.getPostParams();
                if (params != null) {
                    for (var param : params) {
                        System.out.println(param.getKey() + " = " + param.getValue() + "\n");
                    }
                }
            }
        });

        server.addHandler("/classic.html", Method.GET, new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) {
                final String template;
                final var filePath = Path.of(".", "public", request.getPath());
                try {
                    template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + Files.probeContentType(filePath) + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.start();
    }

    static void sendFile(Request request, BufferedOutputStream responseStream, Path path) {
        try {
            var mimeType = Files.probeContentType(path);
            var buffer = Files.readString(path);
            var response = (
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + buffer.getBytes().length + "\r\n" +
                    "Connection: close\r\n\r\n" +
                    buffer).getBytes();
            responseStream.write(response);
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}