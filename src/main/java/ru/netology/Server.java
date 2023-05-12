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
    private int memoryForRequestReading;

    {
        validPaths.add("/index.html");
        validPaths.add("/spring.svg");
        validPaths.add("/resources.html");
        validPaths.add("/spring.png");
        validPaths.add("/styles.css");
        validPaths.add("/app.js");
        validPaths.add("/links.html");
        validPaths.add("/forms.html");
        validPaths.add("/classic.html");
        validPaths.add("/events.html");
        validPaths.add("/events.js");

    }

    public Server(int port, int treadQuantity, int memoryForRequestReading) {
        this.port = port;
        this.treadQuantity = treadQuantity;
        this.memoryForRequestReading = memoryForRequestReading;
    }

    /*
    1. Запрос (без Query String) уходит на сервер c браузера на локалхост.
    2. Сервер передает в новый поток в параметры метода handle(ServerSocket.accept()).
    3. Тред обращается к мэпе хендлеров, находит нужный хендлер, вызывает у него метод хэндл. Метод хендл записывает ответ.
     */
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
                                 final var inputStream = socket.getInputStream();
                                 final var in = new BufferedReader(new InputStreamReader(inputStream));
                                 final var out = new BufferedOutputStream(socket.getOutputStream())) {
                                System.out.println("new connection");

                                // must be in form GET /path HTTP/1.1
                                final var requestLine = in.readLine();
                                final var parts = requestLine.split(" ");

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
                                System.out.println("парсинг заголовков");
                                String header = in.readLine();
                                var headers = new HashMap<String, String>();
                                while (!header.equals("")) {    //readline("\r\n") возвращает ""
                                    headers.put(header.split(" ")[0], header.split(": ")[1]);
                                    header = in.readLine();
                                }
                                System.out.println(headers);

                                byte[] body;
                                if (headers.containsKey("Content-Length")) {
                                    inputStream.reset();
                                    var buffer = new String(inputStream.readAllBytes());
                                    var startPositionOfBody = buffer.indexOf("\r\n\r\n") + 4;
                                    body = Arrays.copyOfRange(buffer.getBytes(), startPositionOfBody, buffer.length());
                                } else body = new byte[0];

                                var request = new Request(parts, headers, body);

                                // check handlers for the request and handle if exists
                                final var handler = handlerBase.getHandlerIfExists(request);
                                if (handler != null) {
                                    handler.handle(request, out);
                                    return;
                                }

                                final var path = parts[1];
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
                                System.out.println(filePath);
                                // определение тип файла в соотвтествии с реестром IANA
                                final var mimeType = Files.probeContentType(filePath);

                                // special case for classic
                                if (path.equals("/classic.html")) {
                                    final var template = Files.readString(filePath);
                                    final var content = template.replace(
                                            "{time}",
                                            LocalDateTime.now().toString()
                                    ).getBytes();
                                    out.write((
                                            "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: " + mimeType + "\r\n" +
                                            "Content-Length: " + content.length + "\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                                    ).getBytes());
                                    out.write(content);
                                    out.flush();
                                    return;
                                }


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