package com.fastfox.watchassistant;

import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;
import com.excheer.until.PhoneUtils;
import com.excheer.until.SettingConfig;
import com.excheer.watchassistant.version.Version;
import com.excheer.watchassistant.version.VersionModel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CheckUpdateTask extends AsyncTask<Void, Void, Boolean> {
	private Context mContext;
	private VersionModel mVersionModel;

	public CheckUpdateTask(Context context) {
		this.mContext = context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		
		if (SettingConfig.getIsNotifyUpdateToday(mContext)) {
			long lastTime = SettingConfig.getLastNotifiedUpdateTime(mContext);
			long nowTime = System.currentTimeMillis();
			SettingConfig.saveLastNotifiedUpdateTime(mContext, nowTime);
			long deltaTime = nowTime - lastTime;
			if (deltaTime < 24 * 60 * 60 * 1000) {
				Log.d("version","delta time not enough one day");
				//LogUtil.d("delta time not enough one day");
				return false;
			}
		}

		if (!PhoneUtils.isNetAvailable(mContext)) {
			//LogUtil.d("network is not avaiable");
			return false;
		}
		String version = Integer.toString(BLUtils.getAppVersionCode(mContext, 
				"com.excheer.watchassistant"));
		mVersionModel = Version.checkUpdate(mContext,version);
		Log.d("version","mVersionModel:" + mVersionModel);
		
		if(mVersionModel == null 
		        || mVersionModel.getVersionCode() <= 0/* ||
		        mVersionModel.getRetCode() == 0*/){
			Log.d("version","error version model");
		    return false;
		}
		
		int versionCode = PhoneUtils.getAppVersionCode(mContext);
		int newVersionCode = mVersionModel.getVersionCode();
		if (newVersionCode <= versionCode) {
			Log.d("version","no new version");
			return false;
		}

		//LogUtil.d("find new version");
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		Log.d("version"," version check res "+result);
		if (result) {
			showUpdateNotify();
		}
		super.onPostExecute(result);
	}

	private void showUpdateNotify() {
		NotificationManager mNotifyMgr = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mContext);
		builder.setContentText(mContext.getResources().getString(R.string.findVersion)+ mVersionModel.getVersionName());
		builder.setSmallIcon(R.drawable.notify_update);
		
		Bitmap mIconBitmap = BitmapFactory.decodeResource(mContext.getResources(), 
		        R.drawable.notify_large_icon);
		builder.setLargeIcon(mIconBitmap);
		
		builder.setContentTitle(mContext.getString(R.string.app_name));
		builder.setDefaults(Notification.DEFAULT_SOUND);
		builder.setAutoCancel(true);
		
		Log.d("version","---SDK:"+android.os.Build.VERSION.SDK_INT);
		
		Intent intent = new Intent(mContext, NotifyUpdateActivity1.class);
		intent.putExtra("version_model", mVersionModel);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext,
				R.string.app_name, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Log.d("version","---intent NotifyUpdateActivity---");
		builder.setContentIntent(contentIntent);
		
		Notification notification = builder.build();
		mNotifyMgr.notify(Contant.NOTIFY_UPDATE_ID, notification);
	}

}
