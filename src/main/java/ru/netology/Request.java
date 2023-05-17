package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String[] requestLine;
    private String method;
    private String path;
    private HashMap<String, String> headers;
    private byte[] body;
    private List<NameValuePair> queryParams;
    private String queryString;

    // Класс работает только для application/x-www-form-urlencoded
    //https://commons.apache.org/proper/commons-fileupload/using.html - библиотке для multipart/form-date

    public Request(String[] requestLine, HashMap<String, String> headers, byte[] body) {
        this.requestLine = requestLine;
        this.method = requestLine[0];
        //парсинг queryString
        if (requestLine[1].contains("?")) {
            path = requestLine[1].substring(0, requestLine[1].indexOf("?"));
            queryString = requestLine[1].substring(requestLine[1].indexOf("?") + 1);
            queryParams = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
        } else {
            path = requestLine[1];
        }
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

    String getQueryParam(String name) {
        if (queryParams != null) {
            for (NameValuePair pair : queryParams) {
                if (pair.getName().equals(name)) {
                    return pair.getValue();
                }
            }
        }
        return null;
    }

    List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    List<Map.Entry<String, String>> getPostParams(){
        if (body.length != 0) {
            List<Map.Entry<String, String>> params = new ArrayList<>();
            String[] pairs = new String(body).split("&");
            for (String pair : pairs) {
                String key = URLDecoder.decode(pair.split("=")[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.split("=")[1], StandardCharsets.UTF_8);
                params.add(Map.entry(key, value));
            }
            return params;
        } else {
            return null;
        }
    }

    String getPostParam(String name) {
        var params = getPostParams();
        if (params != null) {
            for (var param : params) {
                if (param.getKey().equals(name)) {
                    return param.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        var request = new StringBuilder(method + " " + requestLine[1] + " " + requestLine[2] + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        return request.append("\n").toString();
    }
}