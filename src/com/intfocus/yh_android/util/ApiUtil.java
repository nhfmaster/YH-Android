package com.intfocus.yh_android.util;

import org.OpenUDID.*;

import java.util.HashMap;
import java.util.Map;

import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;

import org.apache.commons.httpclient.params.HttpMethodParams;


public class ApiUtil {

	// {device: {name, platform, os, os_version, uuid}}
	public static void authentication(String username, String password) {
		String urlString = String.format(URLs.ApiLogin, "android", username, password);
		Map<String, String> device = new HashMap();
		device.put("name", "hell");
		device.put("platform", "android");
		device.put("os", "hell");
		device.put("os_version", "hell");
		device.put("uuid", OpenUDID_manager.getOpenUDID());
		HttpMethodParams params = new HttpMethodParams();
		params.setParameter("device", device);
		HttpUtil.httpPost(urlString, params, false);
	}
}
