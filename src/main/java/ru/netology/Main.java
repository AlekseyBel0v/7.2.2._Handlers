package ru.netology;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        //Создание файла конфигураций с номером порта и количеством потоков, которые обрабатывают запросы на сервере
        int port = 9999;
        int threadQuantity = 64;
        int memoryForRequestReading = 1024;
        final var configs = Path.of("src/main/resources/configs.csv");
        if (!Files.exists(configs)) {
            try {
                Files.createFile(configs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (final var bos = Files.newBufferedWriter(configs)) {
                bos.write("port:" + port + "\n" +
                          "thread quantity:" + threadQuantity + "\n" +
                          "memory for request reading:" + memoryForRequestReading);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (final var bis = Files.newBufferedReader(configs)) {
            port = Integer.parseInt(bis.readLine().split(":")[1]);
            threadQuantity = Integer.parseInt(bis.readLine().split(":")[1]);
            memoryForRequestReading = Integer.parseInt(bis.readLine().split(":")[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        File configs = new File("src/main/resources/configs.csv");
//        if (!configs.exists()) {
//            try {
//                configs.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try (BufferedWriter bw = new BufferedWriter(new FileWriter(configs))) {
//                bw.write("port:" + port + "\n" +
//                         "thread quantity:" + threadQuantity + "\n" +
//                         "memory for request reading:" + memoryForRequestReading);
//                bw.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        // чтение файла конфигураций
//        try (BufferedReader br = new BufferedReader(new FileReader(configs))) {
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        var server = new Server(port, threadQuantity, memoryForRequestReading);
        server.addHandler("/message", Method.GET, new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                System.out.println("Hello from GET!");
            }
        });

        server.addHandler("/message", Method.PUT, new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                System.out.println("Hello from PUT!");
            }
        });

        server.start();
    }
}