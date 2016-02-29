package fr.univ_savoie.mamiejeanne.utils;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.HttpEntity;

public class HttpClient {
    private static final String BASE_URL = "http://" + Constants.LIGHT_IP;
    public static String uri = "";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, HttpEntity params, String s, AsyncHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), params, s, responseHandler);
    }

    public static void put(Context context, String url, HttpEntity params, String s, AsyncHttpResponseHandler responseHandler) {
        client.put(context, getAbsoluteUrl(url), params, s, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
