package com.fastfox.watchassistant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.ElasticScrollView.view.DrawView;
import com.ElasticScrollView.view.ElasticScrollView;
import com.ElasticScrollView.view.MyChartView;
import com.ElasticScrollView.view.ElasticScrollView.OnRefreshListener;
import com.ElasticScrollView.view.MyChartView.Mstyle;
import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends Activity{
//	ElasticScrollView elasticScrollView;
	private ImageView button,sync;
	private TextView text;
	
	// private 
	private TextView mDevicePower;
	private TextView mSyncTime;
	private TextView mDeviceVersion;
	private TextView mAppVersion;
	
	private TextView mRebindBtn;
	private Button mPhone_status;
	private Boolean mCall_flag;
	private void syncWithConfig(){
		mDevicePower.setText(User.getPowerPercent(AboutActivity.this)+"%");
		mDeviceVersion.setText(User.getDeviceVersion(AboutActivity.this)+"");
		mAppVersion.setText(BLUtils.getAppVersionName(AboutActivity.this));
		
		//mSyncTime
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date(User.getSyncTime(AboutActivity.this));
		  String dateString = formatter.format(now);
		  mSyncTime.setText(dateString);
	}
	 public void onCreate(Bundle savedInstanceState) {  
         super.onCreate(savedInstanceState);  
         setContentView(R.layout.about);
         Log.d("tt","FourActivity onCreate");
         TabMainActivity tab = (TabMainActivity) getParent();
  	   	 tab.handler.obtainMessage(104).sendToTarget();
         LayoutInflater flater = LayoutInflater.from(this);
//         elasticScrollView = (ElasticScrollView)findViewById(R.id.scrollview1);
         
         FrameLayout layout = new FrameLayout(this);
         LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
        		 LayoutParams.MATCH_PARENT,
        		 LayoutParams.MATCH_PARENT);
         layout.setLayoutParams(param);         
//         View about = flater.inflate(R.layout.about, null);
         
         mDevicePower = (TextView)findViewById(R.id.device_power);
         mSyncTime = (TextView)findViewById(R.id.sync_time);
         mDeviceVersion = (TextView)findViewById(R.id.device_version);
         mAppVersion = (TextView)findViewById(R.id.app_version);
         mPhone_status = (Button)findViewById(R.id.phone_status);
         mRebindBtn = (Button) findViewById(R.id.rebind_btn);
         mCall_flag = BLUtils.getBooleanValue(AboutActivity.this, "phone_flag", true);
         Log.d("debug23","mCall_flag:"+mCall_flag);
         if(mCall_flag){
        	 mPhone_status.setBackgroundResource(R.drawable.call_on);
         }else {
        	 mPhone_status.setBackgroundResource(R.drawable.call_off);
         }
         mRebindBtn.setOnClickListener( new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				createDialog(AboutActivity.this).show();
//				BLUtils.setStringValue(AboutActivity.this,
//						"bindmacaddr", "");
//				BLUtils.setStringValue(AboutActivity.this,
//					"bindname", "");
//				
//				  Intent intent = new Intent(AboutActivity.this, DeviceScanActivity.class);
//	              startActivity(intent);
//	              finish();
	              
			}
        	 
         });
         mPhone_status.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("debug23","---OnClick---"+mCall_flag);
				if(mCall_flag){
		        	 mPhone_status.setBackgroundResource(R.drawable.call_off);
		        	 mCall_flag = false;
		         }else {
		        	 mPhone_status.setBackgroundResource(R.drawable.call_on);
		        	 mCall_flag = true;
		         }
				BLUtils.setBooleanValue(AboutActivity.this, "phone_flag", mCall_flag);
			}
        	 
         });
//         layout.addView(about);
//         elasticScrollView.addChild(layout,1);
//         elasticScrollView.smoothScrollTo(0, 0);        
//         final Handler handler = new Handler() {
//         	public void handleMessage(Message message) {
//         		String str = (String)message.obj;
//         		OnReceiveData(str);
//         	}
//         };
//         elasticScrollView.setonRefreshListener(new OnRefreshListener() {
//  			
//  			@Override
//  			public void onRefresh() {
//  				Thread thread = new Thread(new Runnable() {
//  					
//  					@Override
//  					public void run() {
//  						try {
//  							Thread.sleep(2000);
//  						} catch (InterruptedException e) {
//  							e.printStackTrace();
//  						}
//  						Message message = handler.obtainMessage(0, "new Text");
//  						handler.sendMessage(message);
//  					}
//  				});
//  				thread.start();
//  			}
//  		});
         syncWithConfig();
         LinearLayout data_linearlayout = (LinearLayout)findViewById(R.id.data_linearlayout);
         LinearLayout sync_linearlayout = (LinearLayout)findViewById(R.id.sync_linearlayout);
         data_linearlayout.setVisibility(View.GONE);
			sync_linearlayout.setVisibility(View.GONE);
         TextView titleView = (TextView)findViewById(R.id.title);
         titleView.setText(R.string.about_title); 
      }  
//  	protected void OnReceiveData(String str) {
////  		TextView textView =  new TextView(this);
////  		textView.setText(str);
////  		elasticScrollView.addChild(textView, 1);
//  		elasticScrollView.onRefreshComplete();
//  	}
//  	
  	public  Dialog createDialog(final Context context) {  
        LayoutInflater inflater = LayoutInflater.from(context);  
        View v = inflater.inflate(R.layout.devices_dialog, null);// 得到加载view  
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.devices_dialog_view);// 加载布局  
        // main.xml中的ImageView  
        TextView cancel = (TextView)v.findViewById(R.id.alter_devicesname);
        TextView unblind_devices = (TextView)v.findViewById(R.id.unblind_devices);
        
        
        final Dialog devicesgDialog = new Dialog(context, R.style.devices_dialog);// 创建自定义样式dialog  
  
        devicesgDialog.setCancelable(true);// 可以用“返回键”取消  
        devicesgDialog.setContentView(layout, new LinearLayout.LayoutParams(  
                LinearLayout.LayoutParams.WRAP_CONTENT,  
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局  
        cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("Login","---alter_devicesname---");
				
	        	devicesgDialog.dismiss();
			}
        	
        });
        unblind_devices.setOnClickListener(new OnClickListener(){

			@SuppressLint("SdCardPath")
			@Override
			public void onClick(View arg0) {
				Log.d("Login","---unblind_devices---");
				BLUtils.setStringValue(AboutActivity.this,
						"bindmacaddr", "");
				BLUtils.setStringValue(AboutActivity.this,
					"bindname", "");
//				File dbFile = new File("/data/data/com.excheer.watchassistant/databases/personal.db");
//				dbFile.delete();
				StepFacade.deleteAll(AboutActivity.this);
				Intent intent = new Intent(AboutActivity.this, DeviceScanActivity.class);
	            startActivity(intent);
	            finish();
				
			}
        });
        return devicesgDialog;  

  	} 
}
