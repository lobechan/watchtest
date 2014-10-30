package com.excheer.watchassistant.version;

import java.io.File;
import java.util.Date;

import org.json.JSONObject;

import com.excheer.until.HttpChannel;
import com.excheer.until.SettingConfig;
import com.fastfox.watchassistant.Contant;
import com.fastfox.watchtest.R;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


public class Version {

	public static VersionModel checkUpdate(Context context, String version) {
		//String sn = ActivationConfig.getWatchSN(context);
		HttpChannel httpChannel = HttpChannel.getInstance();
		String url = Contant.URL_CHECK_UPDATE + "pkgname=com.excheer.watchassistant&format=json&version="+version;
		
		String result = httpChannel.get(url, null);
		Log.d("version","version check url "+url+" res "+result);
		return parseCheckUpdateResult(result);
	}

	private static VersionModel parseCheckUpdateResult(String result) {
		VersionModel model = new VersionModel();
		try {
			JSONObject json = new JSONObject(result);
			if (json.has("RetCode")) {
				Log.d("version","RetCode check ok");
				model.setRetCode(json.getInt("RetCode"));
				if(model.getRetCode() == 1) {
					Log.d("version","getRetCode() == 0 check ok");
					if (json.has("Info")) {
						Log.d("version","Info check ok");
						JSONObject info = json.getJSONObject("Info");
						if (info.has("Status")) {
							model.setStatus(info.getInt("Status"));
						}
						if (info.has("Version")) {
							model.setVersionCode(info.getInt("Version"));
						} 
						if (info.has("VersionName")) {
							model.setVersionName(info.getString("VersionName"));
						}
						if (info.has("Url")) {
							model.setUrl(info.getString("Url"));
						}
						if (info.has("ApkDesc")) {
							model.setDescription(info.getString("ApkDesc"));
						}
						if (info.has("Tag")) {
							model.setTag(info.getString("Tag"));
						}
						if (info.has("ForceUpdate")) {
							model.setForceUpdate(info.getBoolean("ForceUpdate"));
						}
					}
				} else {
					
				}
				
				
			} else {
				model.setRetCode(0);
				return model;
			}
			

			
		} catch (Exception e) {
		Log.d("version","--- ERROR ---" + e.getMessage());
			return model;
		}
		return model;
	}

	public static String downApk(Context context, String url) {
		File dir = Environment.getExternalStorageDirectory();

		String fastfox = dir.getPath() + "/fastfox/";
		File file = new File(fastfox);
		if (!file.exists())
			file.mkdir();

		String lastFileName = fastfox + "/"
				+ (new Date()).toString().hashCode() + ".apk";
		DownloadManager mana = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Request dwreq = new DownloadManager.Request(Uri.parse(url));
		dwreq.setShowRunningNotification(true);
		dwreq.setMimeType("application/vnd.android.package-archive");
		dwreq.setVisibleInDownloadsUi(true);
		dwreq.setTitle(context.getResources().getString(R.string.app_name)); // 用于信息查看
		dwreq.setDescription(context.getResources().getString(R.string.click_to_install)); // 用于信息查看
		File lastFile = new File(lastFileName);
		dwreq.setDestinationUri(Uri.fromFile(lastFile));
		long downloadId = mana.enqueue(dwreq);

		if (downloadId > 0) {
			// Utils.
			// setStringValue(a, "lastdownloadid", String.valueOf(downloadId));
			SettingConfig.saveLastDownloadId(context, downloadId);
		}
		return fastfox;
	}

}
