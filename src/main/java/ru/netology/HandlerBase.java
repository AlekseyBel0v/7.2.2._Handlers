package ru.netology;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerBase {
    // Мэпа1 в ключах хранит пути, а в значениях потокобезопасную мепу2.
    // Мэпа2 хранит в ключах метод, а в значениях обработчик.
    private HashMap<String, ConcurrentHashMap<Method, Handler>> handlers = new HashMap<>();

    void addHandler(String path, Method method, Handler handler) throws RuntimeException {
        if (handlers.containsKey(path) && handlers.get(path).containsKey(method)) {
            throw new RuntimeException("Обработчик для пути " + path + " и метода " + method + "не добавлен, т.к. он уже существуюет");
        }
        if (handlers.containsKey(path)) {
            handlers.get(path).put(method, handler);
        } else {
            ConcurrentHashMap<Method, Handler> newHandler = new ConcurrentHashMap<>();
            newHandler.put(method, handler);
            handlers.put(path, newHandler);
        }
    }

    Handler getHandlerIfExists(Request request) {
        // если в перечислении нет метода из запроса, то и в базе обработчиков его не будет. возвращаем налл
        if (Method.getMethodIfExists(request.getMethod()) == null) {
            return null;
        }
        // если путь с методом запроса есть в базе, возвращаем хендлер
        if (handlers.containsKey(request.getPath()) &&
            handlers.get(request.getPath()).containsKey(Method.valueOf(request.getMethod()))) {
            return handlers.get(request.getPath()).get(Method.valueOf(request.getMethod()));
        } else return null;
    }
}