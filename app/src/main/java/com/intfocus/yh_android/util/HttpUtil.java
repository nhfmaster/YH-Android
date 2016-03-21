package com.intfocus.yh_android.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * ִ执行一个HTTP GET请求，返回请求响应的HTML
     *
     * @param urlString 请求的URL地址
     * @return 返回请求响应的HTML
     */
    //@throws UnsupportedEncodingException
    public static Map<String, String> httpGet(String urlString, Map<String, String> headers) {
        Log.i("HttpMethod#Get", urlString);
        Map<String, String> retMap = new HashMap<>();
        OkHttpClient client = new OkHttpClient();
        Request request;
        Response response;
        if (headers.containsKey("ETag") && headers.containsKey("Last-Modified")) {
            request = new Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", HttpUtil.webViewUserAgent())
                    .addHeader("IF-None-Match", headers.get("ETag"))
                    .addHeader("If-Modified-Since", headers.get("Last-Modified"))
                    .build();
        } else if (headers.containsKey("ETag")) {
            request = new Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", HttpUtil.webViewUserAgent())
                    .addHeader("IF-None-Match", headers.get("ETag"))
                    .build();
        } else if (headers.containsKey("Last-Modified")) {
            request = new Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", HttpUtil.webViewUserAgent())
                    .addHeader("If-Modified-Since", headers.get("Last-Modified"))
                    .build();
        } else {
            request = new Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", HttpUtil.webViewUserAgent())
                    .build();
        }

        try {

            response = client.newCall(request).execute();

            Headers responseHeaders = response.headers();
            int headerSize = responseHeaders.size();
            for (int i = 0; i < headerSize; i++) {
                retMap.put(responseHeaders.name(i), responseHeaders.value(i));
                Log.i("HEADER", String.format("Key : %s, Value: %s", responseHeaders.name(i), responseHeaders.value(i)));
            }

            int code = response.code();
            Log.i("CODE", code + "");
            retMap.put("code", String.format("%d", code));

            if (code == 200) {
                String responseBody = response.body().string();
                retMap.put("body", responseBody);
                Log.i("responseBody", retMap.get("body"));
            }

        } catch (Exception e) {
            Log.i("GETBUG", e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("timed out")) {
                retMap.put("code", "408");
                retMap.put("body", "{\"info\": \"连接超时\"}");
            } else if (e.getMessage().contains("Unable to resolve host")) {
                retMap.put("code", "400");
                retMap.put("body", "{\"info\": \"网络未连接\"}");
            }
        }
        return retMap;
    }

    /**
     * ִ执行一个HTTP POST请求，返回请求响应的HTML
     */
    //@throws UnsupportedEncodingException
    //@throws JSONException
    public static Map<String, String> httpPost(String urlString, Map params){
        Log.i("HttpMethod#Post", urlString);
        Map<String, String> retMap = new HashMap<>();
        OkHttpClient client = new OkHttpClient();
        Request request;
        Response response;
        Request.Builder requestBuilder = new Request.Builder();

        if (params != null) {
            try {
                Iterator iter = params.entrySet().iterator();
                JSONObject holder = new JSONObject();

                while (iter.hasNext()) {
                    Map.Entry pairs = (Map.Entry) iter.next();
                    String key = (String) pairs.getKey();

                    if (pairs.getValue() instanceof Map) {
                        Map m = (Map) pairs.getValue();

                        JSONObject data = new JSONObject();
                        for (Object o : m.entrySet()) {
                            Map.Entry pairs2 = (Map.Entry) o;
                            data.put((String) pairs2.getKey(), pairs2.getValue());
                            holder.put(key, data);
                        }
                    } else {
                        holder.put(key, pairs.getValue());
                    }
                }
                requestBuilder.post(RequestBody.create(JSON, holder.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            request = requestBuilder
                    .url(urlString)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-type", "application/json")
                    .addHeader("User-Agent", HttpUtil.webViewUserAgent())
                    .build();
            response = client.newCall(request).execute();

            Headers responseHeaders = response.headers();
            int headerSize = responseHeaders.size();
            for (int i = 0; i < headerSize; i++) {
                retMap.put(responseHeaders.name(i), responseHeaders.value(i));
                Log.i("HEADER", String.format("Key : %s, Value: %s", responseHeaders.name(i), responseHeaders.value(i)));
            }

            int code = response.code();
            Log.i("CODE", code + "");
            retMap.put("code", String.format("%d", code));

            String responseBody = response.body().string();
            retMap.put("body", responseBody);
            Log.i("responseBody", retMap.get("body"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return retMap;
    }


    /**
     * ִ执行一个HTTP POST请求，返回请求响应的HTML
     */
    public static Map<String, String> httpPost(String urlString, JSONObject params) {
        Log.i("HttpMethod#Post2", urlString);

        Map<String, String> retMap = new HashMap<>();
        OkHttpClient client = new OkHttpClient();
        Request request;
        Response response;
        Request.Builder requestBuilder = new Request.Builder();

        if (params != null) {
                requestBuilder.post(RequestBody.create(JSON, params.toString()));
        }
        try {
            request = requestBuilder
                    .url(urlString)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-type", "application/json")
                    .addHeader("User-Agent", HttpUtil.webViewUserAgent())
                    .build();
            response = client.newCall(request).execute();

            Headers responseHeaders = response.headers();
            int headerSize = responseHeaders.size();
            for (int i = 0; i < headerSize; i++) {
                retMap.put(responseHeaders.name(i), responseHeaders.value(i));
                Log.i("HEADER", String.format("Key : %s, Value: %s", responseHeaders.name(i), responseHeaders.value(i)));
            }

            int code = response.code();
            Log.i("CODE", code + "");
            retMap.put("code", String.format("%d", code));

            String responseBody = response.body().string();
            retMap.put("body", responseBody);

            Log.i("responseBody", retMap.get("body"));
        } catch (IOException e) {
            e.printStackTrace();
            // 400: Unable to resolve host "yonghui.idata.mobi": No address associated with hostname

            Log.i("DDEBUG", e.getMessage());
            if (e.getMessage().contains("Unable to resolve host")) {
                retMap.put("code", "400");
                retMap.put("body", "{\"info\": \"网络未连接\"}");
            } else if (e.getMessage().contains("Unauthorized")) {
                retMap.put("code", "401");
                retMap.put("body", "{\"info\": \"用户名或密码错误\"}");
            }
        }
        return retMap;
    }

    public static String UrlToFileName(String urlString) {
        String path = "default";
        try {
            urlString = urlString.replace(URLs.HOST, "");
            URI uri = new URI(urlString);
            path = uri.getPath().replace("/", "_");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%s.html", path);
    }

    private static String webViewUserAgent() {
        String userAgent = System.getProperty("http.agent");
        if (userAgent == null) {
            userAgent = "Mozilla/5.0 (Linux; U; Android 4.3; en-us; HTC One - 4.3 - API 18 - 1080x1920 Build/JLS36G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30 default-by-hand";
        }

        return userAgent;
    }

}