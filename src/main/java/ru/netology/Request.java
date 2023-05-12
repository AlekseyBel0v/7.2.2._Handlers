package ru.netology;

import java.util.HashMap;

public class Request {
    private String [] requestLine = new String[2];
    private String method;
    private String path;
    private HashMap<String, String> headers = new HashMap<>();
    private byte[] body;

    public Request(String [] requestLine, HashMap<String, String> headers, byte[] body) {
        this.requestLine = requestLine;
        this.method = requestLine[0];
        this.path = requestLine[1];
        this.headers = headers;
        this.body = body;
    }

    public String[] getRequestLine() {
        return requestLine;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
