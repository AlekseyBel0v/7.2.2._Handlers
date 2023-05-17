package ru.netology;

import org.apache.hc.core5.http.NameValuePair;

import java.io.BufferedOutputStream;
import java.util.List;

public class HandlerForMessageGET implements Handler {
    @Override
    public void handle(Request request, BufferedOutputStream responseStream) {
        Main.answerMessageHTML(request, responseStream);
        System.out.println("Hello from get!");
        printQueryStringIfExist(request.getQueryParams());
    }

    static void printQueryStringIfExist(List<NameValuePair> queryParams) {
        if (queryParams != null) {
            System.out.println("Параметры из query string: \n");
            for (NameValuePair pair : queryParams) {
                System.out.println(pair.getName() + " = " + pair.getValue() + "\n");
            }
        }
    }
}
