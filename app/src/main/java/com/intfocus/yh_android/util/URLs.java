package com.intfocus.yh_android.util;

import android.os.Environment;

import java.io.Serializable;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * api链接，宏
 * @author jay
 * @version 1.0
 * @created 2016-01-06
 */
public class URLs implements Serializable {
	
	
	public final static String HOST = "http://yonghui.idata.mobi";
	public final static String HOST1  = "http://10.0.3.2:4567";

	//login
    public final static String LOGIN_PATH              = String.format("%s/mobile/login", HOST);
    public final static String API_USER_PATH           = "%s/api/v1/%s/%s/%s/authentication";


    public final static String API_DATA_PATH           = "/api/v1/group/%s/report/%s/attachment";
    public final static String API_COMMENT_PATH        = "/api/v1/user/%d/id/%d/type/%d";
    public final static String API_SCREEN_LOCK_PATH    = "/api/v1/user_device/%s/screen_lock";
    public final static String API_DEVICE_STATE_PATH   = "/api/v1/user_device/%s/state";
    public final static String API_RESET_PASSWORD_PATH = "/api/v1/update/%s/password";

    public final static String KPI_PATH                = "/mobile/role/%s/group/%s/kpi";
    public final static String MESSAGE_PATH            = "/mobile/role/%s/user/%s/message";
    public final static String APPLICATION_PATH        = "/mobile/role/%s/app";
    public final static String ANALYSE_PATH            = "/mobile/role/%s/analyse";
    public final static String COMMENT_PATH            = "/mobile/id/%s/type/%s/comment";
    public final static String RESET_PASSWORD_PATH     = "/mobile/update_user_password";

    public final static String FONTS_PATH              = "/mobile/assets/fonts.zip";

    public final static String REPORT_DATA_FILENAME    = "template_data_group_%s_report_%s.js";


	public final static String STORAGE_BASE              = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final static String TimeStamp                 = new SimpleDateFormat("yyyyMMddKKmmss").format(new Date());

    /*
     *  sd path: /storage/emulated/0
     *  /storage/emulated/0/Shared/{assets,loading}
     *  /storage/emulated/0/user.plist
     *  /storage/emulated/0/user-(user-id)/{config, HTML}
     */
	public final static String USER_CONFIG_FILENAME      = "user.plist";
	public final static String CONFIG_DIRNAME            = "Configs";
	public final static String SETTINGS_CONFIG_FILENAME  = "Setting.plist";
	public final static String TABINDEX_CONFIG_FILENAME  = "page_tab_index.plist";
	public final static String GESTURE_PASSWORD_FILENAME = "gesture_password.plist";
	public final static String HTML_DIRNAME              = "HTML";
	public final static String ASSETS1_DIRNAME           = "Assets";
	public final static String SHARED_DIRNAME            = "Shared";

	public final static String CACHED_HEADER_FILENAME    = "cached_header.plist";
    public final static String CURRENT_VERSION__FILENAME = "current_version.txt";
	

	
	
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