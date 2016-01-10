package com.intfocus.yh_android.util;

import android.os.Environment;

import java.io.Serializable;
import java.net.URLEncoder;
import java.security.MessageDigest;

/**
 * api链接，宏
 * @author jay
 * @version 1.0
 * @created 2016-01-06
 */
public class URLs implements Serializable {
	
	
	public final static String HOST1 = "http://yonghui.idata.mobi";
	public final static String HOST  = "http://10.0.3.2:4567";

	//login
    public final static String UILogin = String.format("%s/mobile/login", HOST1);
    public final static String ApiLogin = "%s/api/v1/%s/%s/%s/authentication";


	public final static String STORAGE_BASE              = Environment.getExternalStorageDirectory().getAbsolutePath();

	public final static String USER_CONFIG_FILENAME      = "user.plist";
	public final static String CONFIG_DIRNAME            = "Configs";
	public final static String SETTINGS_CONFIG_FILENAME  = "Setting.plist";
	public final static String TABINDEX_CONFIG_FILENAME  = "PageTabIndex.plist";
	public final static String GESTURE_PASSWORD_FILENAME = "GesturePassword.plist";
	public final static String HTML_DIRNAME              = "HTML";
	public final static String ASSETS1_DIRNAME           = "Assets";
	public final static String SHARED_DIRNAME            = "Shared";

	public final static String CACHED_HEADER_FILENAME    = "cachedHeader.plist";
	

	
	
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
	
	/**
     * MD5加码。32位
     * @param inStr
     * @return
     */
    public static String MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
 
        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
 
        byte[] md5Bytes = md5.digest(byteArray);
 
        StringBuffer hexValue = new StringBuffer();
 
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
 
        return hexValue.toString();
    }
}