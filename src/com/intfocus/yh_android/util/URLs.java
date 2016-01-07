package com.intfocus.yh_android.util;


import java.io.Serializable;
import java.net.URLEncoder;

import android.os.Environment;

/**
 * api链接，宏
 * @author jay
 * @version 1.0
 * @created 2016-01-06
 */
public class URLs implements Serializable {
	
	
	public final static String HOST = "http://yonghui.idata.mobi";
	public final static String HOST1 = "http://10.0.3.2:4567";

	//login
	public final static String Login  = String.format("%s/mobile/login", HOST1);


	public final static String STORAGE_BASE     = Environment.getExternalStorageDirectory().getAbsolutePath() 
			+ "/solife.consume/";
	public final static String STORAGE_GRAVATAR = STORAGE_BASE + "gravatar";
	public final static String STORAGE_APK      = STORAGE_BASE + "apk";
	public final static String STORAGE_IMAGES   = STORAGE_BASE + "images";
	
	public final static int URL_OBJ_TYPE_OTHER = 0x000;
	public final static int URL_OBJ_TYPE_NEWS = 0x001;
	public final static int URL_OBJ_TYPE_SOFTWARE = 0x002;
	public final static int URL_OBJ_TYPE_QUESTION = 0x003;
	public final static int URL_OBJ_TYPE_ZONE = 0x004;
	public final static int URL_OBJ_TYPE_BLOG = 0x005;
	public final static int URL_OBJ_TYPE_TWEET = 0x006;
	public final static int URL_OBJ_TYPE_QUESTION_TAG = 0x007;
	
	
	/**
	 * 对URL进行格式处理
	 * @param path
	 * @return
	 */
	private final static String formatURL(String path) {
		if(path.startsWith("http://") || path.startsWith("https://"))
			return path;
		return "http://" + URLEncoder.encode(path);
	}	
}