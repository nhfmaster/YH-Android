package com.intfocus.yh_android.util;

import android.content.Context;

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

    public final static String API_USER_PATH           = "%s/api/v1/%s/%s/%s/authentication";
    public final static String API_DATA_PATH           = "%s/api/v1/group/%s/report/%s/attachment";
    public final static String API_COMMENT_PATH        = "%s/api/v1/user/%d/id/%d/type/%d";
    public final static String API_SCREEN_LOCK_PATH    = "%s/api/v1/user_device/%s/screen_lock";
    public final static String API_DEVICE_STATE_PATH   = "%s/api/v1/user_device/%d/state";
    public final static String API_RESET_PASSWORD_PATH = "%s/api/v1/update/%s/password";
    public final static String API_ACTION_LOG_PATH     = "%s/api/v1/android/logger";
    public final static String API_ASSETS_PATH         = "%s/api/v1/download/assets.zip";

    public final static String LOGIN_PATH              = "%s/mobile/login";
    public final static String KPI_PATH                = "%s/mobile/role/%s/group/%s/kpi";
    public final static String MESSAGE_PATH            = "%s/mobile/role/%s/user/%s/message";
    public final static String APPLICATION_PATH        = "%s/mobile/role/%s/app";
    public final static String ANALYSE_PATH            = "%s/mobile/role/%s/analyse";
    public final static String COMMENT_PATH            = "%s/mobile/id/%s/type/%s/comment";
    public final static String RESET_PASSWORD_PATH     = "%s/mobile/update_user_password";

    public final static String REPORT_DATA_FILENAME    = "template_data_group_%s_report_%s.js";

    // public final static String STORAGE_BASE            = String.format("%s/com.intfocus.yh_android", Environment.getExternalStorageDirectory().getAbsolutePath());
    public final static String TimeStamp               = new SimpleDateFormat("yyyyMMddKKmmss").format(new Date());

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
	public final static String SHARED_DIRNAME            = "Shared";
    public final static String CACHED_DIRNAME            = "Cached";

	public final static String CACHED_HEADER_FILENAME    = "cached_header.plist";
    public final static String CURRENT_VERSION__FILENAME = "current_version.txt";

	public final static String storage_base(Context context) {
        //    String path = "";
        //    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        //        path = String.format("%s/com.intfocus.yh_android", Environment.getExternalStorageDirectory().getAbsolutePath());
        //    } else {
        //        path =String.format("%s/com.intfocus.yh_android", context.getApplicationContext().getFilesDir());
        //    }
        return context.getApplicationContext().getFilesDir().toString();
    }

	/**
	 * 对URL进行格式处理
     *
	 * @param path
	 * @return
	 */
	private final static String formatURL(String path) {
		if(path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
		return "http://" + URLEncoder.encode(path);
	}	
	
	/**
     * MD5加密-32位
     *
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
 
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
 
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