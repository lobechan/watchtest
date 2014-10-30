package com.fastfox.watchassistant;

import android.content.Context;

import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;

public class User {

	public static String getSex(Context context){
		String userName = 
				BLUtils.getStringValue(context, "sex",
						"Male");
		return userName;
	}
	public static boolean setSex(Context context,String sex){
		boolean setres = 
				BLUtils.setStringValue(context, "sex", sex);
		return setres;
	}
	
	public static String getUserName(Context context){
		String userName = 
				BLUtils.getStringValue(context, "user_name",
						context.getResources().getString(R.string.fastfox_user));
		return userName;
	}
	public static boolean setUserName(Context context,String userName){
		boolean setres = 
				BLUtils.setStringValue(context, "user_name", userName);
		return setres;
	}
	
	public static int getStepTarget(Context context){
		int stepTarget = 
				BLUtils.getIntValue(context, "step_target", 10000);
		return stepTarget;
	}
	public static boolean setStepTarget(Context context,int step){
		boolean setres = 
				BLUtils.setIntValue(context, "step_target", step);
		return setres;
	}
	
	public static int getHeight(Context context){
		int height = 
				BLUtils.getIntValue(context, "height", 170);
		return height;
	}
	public static boolean setHeight(Context context,int height){
		boolean setres = 
				BLUtils.setIntValue(context, "height", height);
		return setres;
	}
	
	public static int getWeight(Context context){
		int weight = 
				BLUtils.getIntValue(context, "weight", 65);
		return weight;
	}
	
	public static boolean setWeight(Context context,int weight){
		boolean setres =
				BLUtils.setIntValue(context, "weight", weight);
		return setres;
	}
	
	
	public static int getSleepTarget(Context context){
		int sleep_target = 
				BLUtils.getIntValue(context, "sleep_target", (8*60));
		return sleep_target;
	}
	public static boolean setSleepTarget(Context context, int sleep_target){
		boolean setres = 
				BLUtils.setIntValue(context, "sleep_target", sleep_target);
		return setres;
	}
	
	
	// dynamic data
	
	public static int getPowerPercent(Context context){
		int power_percent = 
				BLUtils.getIntValue(context, "power_percent", 100);
		return power_percent;
	}
	public static boolean setPowerPercent(Context context,int percent){
		boolean setres = 
				BLUtils.setIntValue(context, "power_percent", percent);
		return setres;
	}
	
	public static long getSyncTime(Context context){
		long sync_time = 
				BLUtils.getLongValue(context, "sync_time", (1410955205*1000L));
		return sync_time;
	}
	public static boolean setSyncTime(Context context,long sync_time){
		boolean setres = 
				BLUtils.setLongValue(context, "sync_time", sync_time);
		return setres;
	}
	
	public static int getDeviceVersion(Context context){
		int power_percent = 
				BLUtils.getIntValue(context, "device_version", 28);
		return power_percent;
	}
	public static boolean setDeviceVersion(Context context,int version){
		boolean setres = 
				BLUtils.setIntValue(context, "device_version", version);
		return setres;
	}
	
	public static String getDeviceSerial(Context context){
		String serial = 
				BLUtils.getStringValue(context, "device_serial", "123456789");
		return serial;
	}
	public static boolean setDeviceSerial(Context context, String serial){
		boolean setres =
				BLUtils.setStringValue(context, "device_serial", serial);
		return setres;
	}
}
