package com.fastfox.watchassistant;

import com.excheer.until.SettingConfig;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class UpdateApkSevice extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mDownloadStateReceiver);
		super.onDestroy();
	}

	private void registerReceiver() {
		Log.d("version","register mDownloadState Receiver");
		IntentFilter mDownloadFilter = new IntentFilter();
		mDownloadFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		mDownloadFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		mDownloadFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
		registerReceiver(mDownloadStateReceiver, mDownloadFilter);
	}

	private BroadcastReceiver mDownloadStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

				handleDownComplete(intent);

			}
		}
	};

	private void handleDownComplete(Intent intent) {
		DownloadManager mana = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		//LogUtil.d("---------start handleDownComplete");

		long downId = intent
				.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		long waitid = SettingConfig.getLastDownloadId(this);

		if (waitid != downId) {
			//LogUtil.d("------ download id is error");
			return;
		}

		//LogUtil.d("------ download id is right");
		Query query = new Query();
		query.setFilterById(downId);
		Cursor c = mana.query(query);
		if (c.moveToFirst()) {
			int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
		//	LogUtil.d("columnIndex-->" + columnIndex);
			if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
				//LogUtil.d("download success");
				final String uriString = c.getString(c
						.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

				String mediaString = c.getString(c
						.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
				//LogUtil.d("mediaString==>" + mediaString);

				if (mediaString == null) {
					//LogUtil.d("download is not apk file");
					return;
				}

				// snmsung note media type is application/apk
				if (mediaString
						.endsWith("application/vnd.android.package-archive")
						|| "application/apk".equals(mediaString)) {
					//LogUtil.d("install apk");
					Intent i = new Intent(Intent.ACTION_VIEW);
					String installpath = uriString;
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setDataAndType(Uri.parse(installpath),
							"application/vnd.android.package-archive");
					getApplicationContext().startActivity(i);
					stopSelf();
				}
			}
		}
	}

}
