package com.bluefay.android;

import com.bluefay.android.MessageObsever;
import com.bluefay.core.BLLog;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;

public class MessageApplication extends Application {

	protected MessageObsever mObsever;
	protected static MessageApplication mInstance;
	
	protected boolean mDebugable;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		BLLog.i("onConfigurationChanged");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		BLLog.i("onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		BLLog.i("onTerminate");
	}

	private void init() {
		mInstance = this;
		ApplicationInfo appinfo = getApplicationInfo();
		if (appinfo != null) {
			boolean isDebug = (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			if (isDebug) {
				BLLog.setLevel(BLLog.LEVEL_DEBUG);
			} else {
				BLLog.setLevel(BLLog.LEVEL_INFO);
			}
			String tag = appinfo.className;
			int lastSeparator = tag.lastIndexOf('.');
			if (lastSeparator >= 0) {
				tag = tag.substring(lastSeparator + 1);
			}
			mDebugable = isDebug;
			BLLog.setTag(tag);
			BLLog.i("log isDebug=%s, tag=%s", isDebug, tag);
		}
		BLLog.i("onCreate");
		mObsever = new MessageObsever();
	}

	public static MessageApplication getApplication() {
		return mInstance;
	}

	public static Context getAppContext() {
		return mInstance.getApplicationContext();
	}

	public static MessageObsever getObsever() {
		return mInstance.mObsever;
	}
	
	public static boolean isDebugable() {
		return mInstance.mDebugable;
	}
}
