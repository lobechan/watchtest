package com.excheer.until;

import com.fastfox.watchassistant.Contant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingConfig {
	private static final String PREF_NAME = "com.igeak.sync_preference";
	private static SharedPreferences preferences;

	public static SharedPreferences getPreference(Context context) {
		if (preferences == null) {
			preferences = context.getApplicationContext().getSharedPreferences(
					PREF_NAME, Context.MODE_PRIVATE);
		}
		return preferences;
	}

	private static Editor getEditor(Context context) {
		return getPreference(context).edit();
	}
	/** get last download id */
	public static long getLastDownloadId(Context context) {
		return getPreference(context).getLong(Contant.PRE_LAST_DOWNLOAD_ID, -1L);
	}

	/** save last download id */
	public static void saveLastDownloadId(Context context, long id) {
		Editor editor = getEditor(context);
		editor.putLong(Contant.PRE_LAST_DOWNLOAD_ID, id);
		editor.commit();
	}
	// /////////////////////////////
	
	/** is notify update today */
	public static boolean getIsNotifyUpdateToday(Context context) {
		return getPreference(context).getBoolean(Contant.PRE_IS_NOTIFY_UPDATE_TODAY, false);
	}

	/** save is notify update today */
	public static void saveIsNotifyUpdateToday(Context context, boolean flag) {
		Editor editor = getEditor(context);
		editor.putBoolean(Contant.PRE_IS_NOTIFY_UPDATE_TODAY, flag);
		editor.commit();
	}
	
	/** has been notified update today */
	public static long getLastNotifiedUpdateTime(Context context) {
		return getPreference(context).getLong(Contant.PRE_LAST_NOTIFIED_UPDATE_TIME, 0L);
	}

	/** save has been notified update today */
	public static void saveLastNotifiedUpdateTime(Context context, long date) {
		Editor editor = getEditor(context);
		editor.putLong(Contant.PRE_LAST_NOTIFIED_UPDATE_TIME, date);
		editor.commit();
	}


}
