package com.intfocus.yh_android.util;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.net.URI;


/**
 * 
 * 
 * <p>Title:HttpTookitEnhance</p>
 * <p>Description: httpclient模拟http请求，解决返回内容乱码问题</p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: </p>
 * @author libin
 * @version 1.0.0
 */
public class HttpUtil {
      /**
       * ִ执行一个HTTP GET请求，返回请求响应的HTML
       *
       * @param url                 请求的URL地址
       * @return                    返回请求响应的HTML
       */
      //@throws UnsupportedEncodingException
      public static Map<String, String> httpGet(String urlString) {
          Log.i("URLString", urlString);
          Map<String, String> retMap = new HashMap();

          DefaultHttpClient client = new DefaultHttpClient();
          HttpGet request = new HttpGet(urlString);
          HttpResponse response;
          try {
              String userAgent = "Mozilla/5.0 (Linux; U; Android 4.3; en-us; HTC One - 4.3 - API 18 - 1080x1920 Build/JLS36G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
              //request.setHeader("Accept", "application/json");
              //request.setHeader("Content-type", "application/json");
              request.setHeader("User-Agent", userAgent);
                response = client.execute(request);
                int code = response.getStatusLine().getStatusCode();
                retMap.put("code", String.format("%d", code));
                if(code == 200) {
                    ResponseHandler<String> handler = new BasicResponseHandler();
                    String responseBody = handler.handleResponse(response);
                    retMap.put("body", responseBody);
                    Log.i("responseBody", responseBody.substring(responseBody.length() - 30));
                }

          } catch (IOException e) {
                e.printStackTrace();
                retMap.put("code", "400");
                retMap.put("body", String.format("%s 访问失败:\n%s", urlString, e.getMessage()));
          }
          return retMap;
      }

      /** 
       * ִ执行一个HTTP POST请求，返回请求响应的HTML
       * 
       * @param url         请求的URL地址
       * @param params      请求的查询参数,可以为null
       * @param charset     字符集
       * @param pretty      是否美化
       * @return            返回请求响应的HTML
       */
      //@throws UnsupportedEncodingException
      //@throws JSONException
      public static Map<String, String> httpPost(String urlString, Map params) throws UnsupportedEncodingException, JSONException {
    	    Log.i("HttpMethod", urlString);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(urlString);
     
            Map<String, String> retMap = new HashMap();
    	    HttpResponse response = null;
            if(params != null) {
              	try {
            	  Iterator iter = params.entrySet().iterator();
                  JSONObject holder = new JSONObject();
                  
                  while(iter.hasNext()) {
                  	Map.Entry pairs = (Map.Entry)iter.next();
                  	String key = (String)pairs.getKey();

                      if(pairs.getValue() instanceof  Map) {
                          Map m = (Map) pairs.getValue();

                          JSONObject data = new JSONObject();
                          Iterator iter2 = m.entrySet().iterator();
                          while (iter2.hasNext()) {
                              Map.Entry pairs2 = (Map.Entry) iter2.next();
                              data.put((String) pairs2.getKey(), (String) pairs2.getValue());
                              holder.put(key, data);
                          }
                      }
                      else {
                          holder.put(key, pairs.getValue());
                      }
                  }

                  StringEntity se = new StringEntity(holder.toString());
                  request.setEntity(se);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            try {
                String userAgent = "Mozilla/5.0 (Linux; U; Android 4.3; en-us; HTC One - 4.3 - API 18 - 1080x1920 Build/JLS36G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");
                request.setHeader("User-Agent", userAgent);
                response = client.execute(request);
                int code = response.getStatusLine().getStatusCode();
                retMap.put("code", String.format("%d", code));

                ResponseHandler<String> handler = new BasicResponseHandler();
                String responseBody = handler.handleResponse(response);
                retMap.put("body", responseBody);
            }
            catch (IOException e) {
            	e.printStackTrace();

                retMap.put("code", "401");
                retMap.put("body", "{\"info\": \"用户名或密码错误\"}");
            }
            finally {
            }
            return retMap;
      }

    public static String UrlToFileName(String urlString) {
        String path = "default";
        try {
            urlString = urlString.replace(URLs.HOST, "");
            URI uri = new URI(urlString);
            path = uri.getPath().replace("/", "_");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return String.format("%s.html", path);
    }
}
