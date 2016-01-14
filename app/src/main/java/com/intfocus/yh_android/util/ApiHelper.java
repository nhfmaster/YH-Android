package com.intfocus.yh_android.util;

import org.OpenUDID.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;

public class ApiHelper {

	// {device: {name, platform, os, os_version, uuid}}
	public static String authentication(String username, String password) {
		String ret = "success";

		String urlString = String.format(URLs.API_USER_PATH, URLs.HOST, "android", username, password);
		try {
    		Map<String, String> device = new HashMap();
    		device.put("name", android.os.Build.MODEL);
    		device.put("platform", "android");
    		device.put("os", android.os.Build.MODEL);
    		device.put("os_version", Build.VERSION.RELEASE);
    		device.put("uuid", OpenUDID_manager.getOpenUDID());
    		Map<String, Map<String, String>> params = new HashMap();
    		params.put("device", device);
    		
			Map<String, String> response = HttpUtil.httpPost(urlString, params);
			
			if(response.get("code").toString().compareTo("200") == 0) {
		        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
				Log.i("userConfigPath", userConfigPath);
				FileUtil.writeFile(userConfigPath, response.get("body").toString());
		       
		        // need user info
				String settingsConfigPath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
				Log.i("settingsConfigPath", settingsConfigPath);
				
				FileUtil.writeFile(settingsConfigPath, response.get("body").toString());
			} else {
				JSONObject json = new JSONObject(response.get("body").toString());
				ret = json.getString("info");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 *  获取报表网页数据
	 */
	public static void reportData(String assetsPath, String groupID, String reportID) {
		String urlPath   = String.format(URLs.API_DATA_PATH, groupID, reportID);
		String urlString = String.format("%s%s", URLs.HOST, urlPath);

		String fileName  = String.format(URLs.REPORT_DATA_FILENAME, groupID, reportID);
		String filePath  = String.format("%s/assets/javascripts/%s", FileUtil.sharedPath(), fileName);

		Map<String, String> response = HttpUtil.httpGet(urlString);

		if(response.get("code").toString().compareTo("200") == 0) {
			try {
				FileUtil.writeFile(filePath, response.get("body").toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.i("Code", response.get("code").toString());
			Log.i("Body", response.get("body").toString());
		}
	}

	/*
	 * 发表评论
	 */
	public static void writeComment(int userID, int objectType, int objectID, Map params) throws UnsupportedEncodingException, JSONException {
		String urlPath   = String.format(URLs.API_COMMENT_PATH, userID, objectID, objectType);
		String urlString = String.format("%s%s", URLs.HOST, urlPath);

		Map<String, String> response = HttpUtil.httpPost(urlString, params);
		Log.i("WriteComment", response.get("code").toString());
		Log.i("WriteComment", response.get("body").toString());
	}



	public static Map<String, String> httpGetWithHeader(String urlString, String assetsPath, String relativeAssetsPath) {
		Map<String, String> retMap = new HashMap<String, String>();

		String urlKey = urlString;
		if (urlString.indexOf("?") != -1) {
			urlKey = TextUtils.split(urlString, "?")[0];
		}
		try {
			String headersFilePath = String.format("%s/%s", assetsPath, URLs.CACHED_HEADER_FILENAME);

			JSONObject headersJSON = new JSONObject();
			if((new File(headersFilePath)).exists()) {
				headersJSON = FileUtil.readConfigFile(headersFilePath);
			}
			JSONObject headerJSON = new JSONObject();

			Map<String, String> headers = new HashMap<String, String>();
			if(headersJSON.has(urlKey)) {
				headerJSON = (JSONObject)headersJSON.get(urlKey);
				if(headerJSON.has("ETag")) {
					headers.put("ETag", headerJSON.getString("ETag"));
				}
				if(headerJSON.has("Last-Modified")) {
					headers.put("Last-Modified", headerJSON.getString("Last-Modified"));
				}
			}

			Map<String, String> response = HttpUtil.httpGet(urlString, headers);
			String statusCode = response.get("code").toString();
			retMap.put("code", statusCode);

			String htmlName = HttpUtil.UrlToFileName(urlString);
			String htmlPath = String.format("%s/%s", assetsPath, htmlName);
			retMap.put("path", htmlPath);

			if (statusCode.compareTo("200") == 0) {
				if(response.containsKey("ETag")) {
					headerJSON.put("ETag", response.get("ETag").toString());
				}
				if(response.containsKey("Last-Modified")) {
					headerJSON.put("Last-Modified", response.get("Last-Modified").toString());
				}

				headersJSON.put(urlKey, headerJSON);
				FileUtil.writeFile(headersFilePath, headersJSON.toString());

				String htmlContent = response.get("body").toString();
				htmlContent = htmlContent.replace("/javascripts/", String.format("%s/javascripts/", relativeAssetsPath));
				htmlContent = htmlContent.replace("/stylesheets/", String.format("%s/stylesheets/", relativeAssetsPath));
				htmlContent = htmlContent.replace("/images/", String.format("%s/images/", relativeAssetsPath));
				FileUtil.writeFile(htmlPath, htmlContent);
			}
		} catch (Exception e) {
			retMap.put("code", "500");
			e.printStackTrace();
		}

		return retMap;
	}
}
