package com.intfocus.yh_android.util;

import org.OpenUDID.*;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;



public class ApiUtil {

	// {device: {name, platform, os, os_version, uuid}}
	public static void authentication(String username, String password) {
		String urlString = String.format(URLs.ApiLogin, URLs.HOST, "android", username, password);
		try {
    		Map<String, String> device = new HashMap();
    		device.put("name", android.os.Build.MODEL);
    		device.put("platform", "android");
    		device.put("os", android.os.Build.MODEL);
    		device.put("os_version", android.os.Build.MODEL);
    		device.put("uuid", OpenUDID_manager.getOpenUDID());
    		Map<String, Map<String, String>> params = new HashMap();
    		params.put("device", device);
    		
			Map<String, String> response = HttpUtil.httpPost(urlString, params, false);
			
			if(response.get("code").toString().compareTo("200") == 0) {
		        String userConfigPath = String.format("%s/%s", FileUtil.basePath(), URLs.USER_CONFIG_FILENAME);
				Log.i("userConfigPath", userConfigPath);
				FileUtil.writeJSON(userConfigPath, response.get("body").toString());
		       
		        // need user info
				String settingsConfigPath = FileUtil.dirPath(URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
				Log.i("settingsConfigPath", settingsConfigPath);
				
				FileUtil.writeJSON(settingsConfigPath, response.get("body").toString());
			}
			else {
				Log.i("Code", response.get("code").toString());
				Log.i("Body", response.get("body").toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
