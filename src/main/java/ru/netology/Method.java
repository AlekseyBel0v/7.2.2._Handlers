package ru.netology;

import java.util.Arrays;

public enum Method {
    GET("GET"),
    PUT("PUT");

    private String method;

    Method(String method) {
        this.method = method;
    }

    static Method getMethodIfExists(String method) {
        if (Arrays.stream(Method.values()).anyMatch(x -> x.toString().equals(method))) {
            return Method.valueOf(method);
        } else return null;
    }
}
