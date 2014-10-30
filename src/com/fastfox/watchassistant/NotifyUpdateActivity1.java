package com.fastfox.watchassistant;

import com.fastfox.watchtest.R;
import com.excheer.until.SettingConfig;
import com.excheer.watchassistant.version.Version;
import com.excheer.watchassistant.version.VersionModel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class NotifyUpdateActivity1 extends Activity implements OnClickListener{

	private TextView versionText;
	private TextView descriptionText;
	private VersionModel mVersionModel;
	private CheckBox isNotifyToday;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notify_update);
		Log.d("version","enter NotifyUpdateActivity");
		findViewById(R.id.btn_update).setOnClickListener(this);
		versionText = (TextView) findViewById(R.id.text_version);
		descriptionText = (TextView) findViewById(R.id.text_description);
		
		isNotifyToday = (CheckBox) findViewById(R.id.check_notify_today);
		isNotifyToday.setChecked(SettingConfig.getIsNotifyUpdateToday(this));
		isNotifyToday.setOnClickListener(this);

		Intent intent = getIntent();
		if (intent != null) {
			mVersionModel = (VersionModel) intent.getSerializableExtra("version_model");
			versionText.setText(mVersionModel.getVersionName());
			descriptionText.setText(mVersionModel.getDescription());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_update:
			downApk();
			break;
		/*case R.id.check_notify_today:
			SettingConfig.saveIsNotifyUpdateToday(this, isNotifyToday.isChecked());
			break;*/
		}
	}

	private void downApk() {
		Toast.makeText(NotifyUpdateActivity1.this, "will download", Toast.LENGTH_SHORT).show();
		startService(new Intent(this, UpdateApkSevice.class));
		new Thread(new Runnable() {

			@Override
			public void run() {
				Version.downApk(NotifyUpdateActivity1.this, mVersionModel.getUrl());
			}
		}).start();
	}
	
}
