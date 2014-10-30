package com.excheer.until;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;


public class PhoneUtils {

	/** is SDCard available */
	public static boolean isSDCardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static int getSimState(Context context) {
		return ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getSimState();
	}

	/** get device Id */
	public static String getDeviceId(Context context) {
		return ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}

	/** get IMSI for GSM */
	public static String getSubscriberId(Context context) {
		return ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
	}

	/** check SIM is foreign */
	public static boolean isForeignSim(Context context) {
		String imsi = getSubscriberId(context);
		return !imsi.startsWith("460");
	}

	/**
	 * check network
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isAvailable()) {
			return true;
		}
		return false;
	}

	/**
	 * check network is connect
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnect(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null) {
			return cm.getActiveNetworkInfo().isConnected();
		}
		return false;
	}

	public static int getAppVersionCode(Context context) {
		int version = 0;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			version = pi.versionCode;
		} catch (Exception e) {
			//LogUtil.d("--- ERROR ---" + e.getMessage());
		}
		return version;
	}

	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
		} catch (Exception e) {
			//LogUtil.d("--- ERROR ---" + e.getMessage());
		}
		return versionName;
	}

}
