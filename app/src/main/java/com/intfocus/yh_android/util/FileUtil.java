package com.intfocus.yh_android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.intfocus.yh_android.util.URLs;
import android.text.TextUtils;

public class FileUtil {
	public static String basePath() {
		return URLs.STORAGE_BASE;
	}
	

	public static String userspace() {
		String nameSpace = "";
		try {
	        String userConfigPath = String.format("%s/%s", URLs.STORAGE_BASE, URLs.USER_CONFIG_FILENAME);
	        JSONObject json = FileUtil.readConfigFile(userConfigPath);
	        
			nameSpace = String.format("%s/user-%d", URLs.STORAGE_BASE, json.getInt("user_id"));
			
			File folder = new File(nameSpace);
			if(!folder.exists() && !folder.isDirectory()) {
				folder.mkdir();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return nameSpace;
	}

	/**
	 *  传递目录名取得沙盒中的绝对路径(一级),不存在则创建，请慎用！
	 *
	 *  @param dirName  目录名称，不存在则创建
	 *
	 *  @return 沙盒中的绝对路径
	 */
	public static String dirPath(String dirName) {
		String pathName = String.format("%s/%s", FileUtil.userspace(), dirName);
		
		try {
			File folder = new File(pathName);
			if(!folder.exists() && !folder.isDirectory()) {
				//folder.mkdir();
				folder.mkdirs();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return pathName;
	}
	
	public static String dirPath(String dirName, String fileName) {
		String pathName = FileUtil.dirPath(dirName);
		
		return String.format("%s/%s", pathName, fileName);
	}

	public static String dirsPath(String[] dirNames) {

		return FileUtil.dirPath(TextUtils.join("/", dirNames));
	}

	/*
	 * 读取本地文件内容
	 */
	public static String readFile(String pathName) {
		String string = null;
		try {
			InputStream inputStream = new FileInputStream(new File(pathName));
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			StringBuilder stringBuilder = new StringBuilder();
			while((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			string = stringBuilder.toString();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return string;
	}

	/*
	 * 读取本地文件内容，并转化为json
	 */
	public static JSONObject readConfigFile(String pathName) {
		JSONObject jsonObject = null;
		try {
			String string = FileUtil.readFile(pathName);
	        jsonObject = new JSONObject(string);
	    } catch (JSONException e) {
	      e.printStackTrace();
	    }
       return jsonObject;
	}

	/*
	 * 字符串写入本地文件
	 */
	public static void writeFile(String pathName, String content) throws IOException {
		Log.i("PathName", pathName);
		File file = new File(pathName);

		if(file.exists()) { file.delete(); }

		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file, true);
		out.write(content.toString().getBytes("utf-8"));
		out.close();
	}

	/*
	 *  共享资源
	 *  1. assets资源
	 *  2. loading页面
	 *  3. 登录缓存页面
	 */
	public static String sharedPath() {
		String pathName = String.format("%s/%s", URLs.STORAGE_BASE, URLs.SHARED_DIRNAME);
		File file = new File(pathName);

		if(!file.exists() && !file.isDirectory()) { file.mkdir(); }

		return pathName;
	}

	/*
	 * 共享资源中的文件（夹）（忽略是否存在）
	 */
	public static String sharedPath(String folderName) {
		if(!folderName.startsWith("/")) {
			folderName = String.format("/%s", folderName);
		}

		String pathName = String.format("%s%s", FileUtil.sharedPath(), folderName);
		return pathName;
	}
}
