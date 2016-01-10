package com.intfocus.yh_android.util;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
//      /**
//       * ִ执行一个HTTP GET请求，返回请求响应的HTML
//       *
//       * @param url                 请求的URL地址
//       * @param queryString         请求的查询参数,可以为null
//       * @param charset             字符集
//       * @param pretty              是否美化
//       * @return                    返回请求响应的HTML
//       */
//      public static String httpGet ( String url, String queryString, String charset, boolean pretty )
//      {
//            StringBuffer response = new StringBuffer();
//            HttpClient client = new HttpClient();
//            HttpMethod method = new GetMethod(url);
//            try
//            {
//                  if ( queryString != null && !queryString.equals("") )
//                        //对get请求参数做了http请求默认编码，好像没有任何问题，汉字编码后，就成为%式样的字符串
//                        method.setQueryString(URIUtil.encodeQuery(queryString));
//                  client.executeMethod(method);
//                  if ( method.getStatusCode() == HttpStatus.SC_OK ) {
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), charset));
//                        String line;
//                        while ( ( line = reader.readLine() ) != null )
//                        {
//                              if ( pretty )
//                                    response.append(line).append(System.getProperty("line.separator"));
//                              else
//                                    response.append(line);
//                        }
//                        reader.close();
//                  }
//            }
//            catch ( URIException e )
//            {
//            }
//            catch ( IOException e )
//            {
//            }
//            finally
//            {
//                  method.releaseConnection();
//            }
//            return response.toString();
//      }

      /** 
       * ִ执行一个HTTP POST请求，返回请求响应的HTML
       * 
       * @param url         请求的URL地址
       * @param params      请求的查询参数,可以为null
       * @param charset     字符集
       * @param pretty      是否美化
       * @return            返回请求响应的HTML
     * @throws JSONException 
       */
      //@throws UnsupportedEncodingException 
      public static Map<String, String> httpPost(String urlString, Map params, boolean pretty ) throws UnsupportedEncodingException, JSONException {
    	    Log.i("HttpMethod", urlString);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost method = new HttpPost(urlString);
     
            Map<String, String> ret = new HashMap();
    	    HttpResponse response = null;
            if ( params != null ) {
              	try {
            	  Iterator iter = params.entrySet().iterator();
                  JSONObject holder = new JSONObject();
                  
                  while(iter.hasNext()) {
                  	Map.Entry pairs = (Map.Entry)iter.next();
                  	String key = (String)pairs.getKey();
                  	Map m = (Map)pairs.getValue();
                  	   
                  	JSONObject data = new JSONObject();
                  	Iterator iter2 = m.entrySet().iterator();
                  	while(iter2.hasNext()) {
                  	Map.Entry pairs2 = (Map.Entry)iter2.next();
						data.put((String)pairs2.getKey(), (String)pairs2.getValue());

		              	holder.put(key, data);
		              }
                  }
                  StringEntity se = new StringEntity(holder.toString());
                  method.setEntity(se);
                  method.setHeader("Accept", "application/json");
                  method.setHeader("Content-type", "application/json");
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            try {
                //ResponseHandler responseHandler = new BasicResponseHandler();
                response = client.execute(method);
                ResponseHandler<String> handler = new BasicResponseHandler();
                String responseBody = handler.handleResponse(response);
                int code = response.getStatusLine().getStatusCode();
                ret.put("code", String.format("%d", code));
                ret.put("body", responseBody);
                
                Log.i("StatusCode", String.format("%d", code));
                Log.i("responseBody", responseBody);
//                JSONObject jsonObject = new JSONObject(responseBody);
//                Log.i("UserInfo", jsonObject.getString("user_name"));
//                Log.i("UserInfo", jsonObject.getString("group_name"));
//                Log.i("UserInfo", jsonObject.getString("role_name"));
            }
            catch ( IOException e ) {
            	e.printStackTrace();
            }
            finally {
            }
            return ret;
      }
}
