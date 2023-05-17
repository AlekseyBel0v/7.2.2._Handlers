package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ArrayList<String> validPaths = new ArrayList<>();
    final private HandlerBase handlerBase = new HandlerBase();
    private int port;
    private int treadQuantity;
    //private int memoryForRequestReading;
    public Server(int port, int treadQuantity) {
        this.port = port;
        this.treadQuantity = treadQuantity;
        //this.memoryForRequestReading = memoryForRequestReading;
    }
    {
        validPaths.add("/index.html");
        validPaths.add("/spring.svg");
        validPaths.add("/resources.html");
        validPaths.add("/spring.png");
        validPaths.add("/styles.css");
        validPaths.add("/app.js");
        validPaths.add("/links.html");
        validPaths.add("/forms.html");
        validPaths.add("/events.html");
        validPaths.add("/events.js");
    }

    void start() {
        System.out.println("server is running");
        ExecutorService threadPool = Executors.newFixedThreadPool(treadQuantity);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("waiting for a connection");
                            try (final var socket = serverSocket.accept();
                                 final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                 final var out = new BufferedOutputStream(socket.getOutputStream())) {
                                System.out.println("new connection");
                                //in.mark(4000); - для информации
                                // must be in form GET /path HTTP/1.1
                                final var requestLine = in.readLine();
                                final var parts = requestLine.split(" ");

                                // проверка реквест лайн
                                if (parts.length != 3) {
                                    out.write((
                                            "HTTP/1.1 400 Bad Request\r\n" +
                                            "Content-Length: 0\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                                    ).getBytes());
                                    out.flush();
                                    return;
                                }

                                // парсинг заголовков;
                                String header = in.readLine();
                                var headers = new HashMap<String, String>();
                                while (!header.equals("")) {    //readline("\r\n") возвращает ""
                                    headers.put(header.split(": ")[0], header.split(": ")[1]);
                                    header = in.readLine();
                                }

                                // парсинг тела
                                byte[] body;
                                if (headers.containsKey("Content-Length")) {
                                    var buffer = new char[Integer.parseInt(headers.get("Content-Length"))];
                                    in.read(buffer);
                                    //in.reset(); - метод для перезагрузки потока данных на позцию отметки (mark())
                                    body = new String(buffer).replaceAll(String.valueOf('\u0000'), "").getBytes();
                                } else body = new byte[0];

                                // Создание объекта Реквест
                                var request = new Request(parts, headers, body);
                                System.out.println("Принят запрос:\n" + request);

                                // handler checking for the request and handle if exists
                                final var handler = handlerBase.getHandlerIfExists(request);
                                if (handler != null) {
                                    handler.handle(request, out);
                                    return;
                                }

                                // проверка существования ресурса
                                final var path = request.getPath();
                                if (!validPaths.contains(path)) {
                                    out.write((
                                            "HTTP/1.1 404 Not Found\r\n" +
                                            "Content-Length: 0\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                                    ).getBytes());
                                    out.flush();
                                    return;
                                }

                                final var filePath = Path.of(".", "public", path);
                                // определение типа файла в соотвтествии с реестром IANA
                                final var mimeType = Files.probeContentType(filePath);
                                final var length = Files.size(filePath);
                                out.write((
                                        "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                                ).getBytes());
                                Files.copy(filePath, out);
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    void addHandler(String path, Method method, Handler handler) {
        validPaths.add(path);
        handlerBase.addHandler(path, method, handler);
    }
}