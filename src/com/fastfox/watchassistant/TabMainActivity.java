package com.fastfox.watchassistant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ElasticScrollView.view.DrawView;
import com.ElasticScrollView.view.ElasticScrollView;
import com.ElasticScrollView.view.ElasticScrollView.OnRefreshListener;
import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;
import com.excheer.protobuf.Device;
import com.excheer.until.Utils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import android.annotation.SuppressLint;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.app.TabActivity;  
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;  
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;  
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;  
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;  
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;  
import android.widget.TabWidget;
import android.widget.TextView;
   
public class TabMainActivity extends TabActivity {  
	
	 public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	 private final static String TAG = TabMainActivity.class.getSimpleName();
	 // excheer protocol
    
	    
    public final static int BEGIN_SYNC_TIMER = 1000;
    public final static int SYNC_OK = 1001;
    public final static int BEGIN_RESYNC_TIMER = 1002;
	private ImageView button,sync;
	private TextView text;
	private View mPopupWindowView;
	private PopupWindow popupWindow;
	private LinearLayout data_linearlayout,sync_linearlayout;
	private TextView action_daydata,action_weekdata,action_mouthdata;
	PopupMenu popup = null;
    /** Called when the activity is first created. */ 
	
	 int sysVersion = Integer.parseInt(VERSION.SDK);
	
	 
	// for ble
	private String mDeviceName;
    private String mDeviceAddress; // after weixin step ok store the data to config!
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private boolean mConnected = false;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    
    private  ServiceConnection mServiceConnection;
    private boolean mWaitingSync = false;
    private static Dialog mLoadingDialog;
    private static boolean incomingFlag = false;
    private static String incoming_number = null;
    private Intent intent1 = null;
    private void initforbleservice(){
    	 mServiceConnection = new ServiceConnection() {

    	        @Override
    	        public void onServiceConnected(ComponentName componentName, IBinder service) {
    	            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
    	            if (!mBluetoothLeService.initialize()) {
    	                Log.e(TAG, "Unable to initialize Bluetooth");
    	                finish();
    	            }
    	            // Automatically connects to the device upon successful start-up initialization.
    	            mBluetoothLeService.connect(mDeviceAddress);
    	            
    	           // mBluetoothLeService.disconnect();
    	        }

    	        @Override
    	        public void onServiceDisconnected(ComponentName componentName) {
    	            mBluetoothLeService = null;
    	        }
    	    };
    	    
    }
    public final static int CACHE_SIZE = 1024;
    byte[] mArray = new byte[CACHE_SIZE];
    private int mRecvCount = 0;
    int mIndex = 0;
    
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("NewApi") @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
               /* updateConnectionState(R.string.connected);
                invalidateOptionsMenu();*/
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
               /* updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();*/
            } else if(Contant.RESYNC_INTENT.equalsIgnoreCase(action)){
            	// try to start sync
            	Toast.makeText(
                		TabMainActivity.this, getResources().getString(R.string.syncing_now), 
                		Toast.LENGTH_SHORT)
                	.show();
            	// 
            	mWaitingSync = true;
            	handler.removeMessages(BEGIN_RESYNC_TIMER);
            	handler.sendEmptyMessageDelayed(BEGIN_RESYNC_TIMER, 40*1000);
            	mLoadingDialog = showLoadingDialog(TabMainActivity.this,""/*TabMainActivity.this.getResources().getString(R.string.syncing)*/);
            	if(!mConnected){
            		if (mBluetoothLeService != null) {
         	            mBluetoothLeService.connect(mDeviceAddress);
         	            
         	        }
            	}
            	
            }else if(Contant.SYNC_OK_INTENT.equalsIgnoreCase(action)){
            	handler.sendEmptyMessage(SYNC_OK);
            	handler.removeMessages(BEGIN_SYNC_TIMER);
				handler.removeMessages(BEGIN_RESYNC_TIMER);
            }else if(Contant.TEST_SUCCEEDED.equalsIgnoreCase(action)){
            	int steps = intent.getIntExtra("teststeps", 0);
            	AlertDialog.Builder builder = new Builder(TabMainActivity.this);
            	builder.setMessage(TabMainActivity.this.getResources().getString(R.string.test_succeeded)+steps);
            	builder.setTitle(TabMainActivity.this.getResources().getString(R.string.test));
            	builder.setPositiveButton(TabMainActivity.this.getResources().getString(R.string.test_sure), new OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						TabMainActivity.this.stopService(intent1);
						BLUtils.setStringValue(TabMainActivity.this,
								"bindmacaddr", "");
						BLUtils.setStringValue(TabMainActivity.this,
							"bindname", "");
						StepFacade.deleteAll(TabMainActivity.this);
						
						arg0.dismiss();
						Intent intent1 = new Intent();
						intent1.setClass(TabMainActivity.this, DeviceScanActivity.class);
						startActivity(intent1);
						TabMainActivity.this.finish();
					}

            	});
            	builder.show();
            }else if(Contant.TEST_FAILED.equalsIgnoreCase(action)){
            	int steps = intent.getIntExtra("teststeps", 0);
            	
            	AlertDialog.Builder builder = new Builder(TabMainActivity.this);
            	builder.setMessage(TabMainActivity.this.getResources().getString(R.string.test_failed)+steps);
            	builder.setTitle(TabMainActivity.this.getResources().getString(R.string.test));
            	builder.setPositiveButton(TabMainActivity.this.getResources().getString(R.string.test_sure), new OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						TabMainActivity.this.stopService(intent1);
						BLUtils.setStringValue(TabMainActivity.this,
								"bindmacaddr", "");
						BLUtils.setStringValue(TabMainActivity.this,
							"bindname", "");
						StepFacade.deleteAll(TabMainActivity.this);
						arg0.dismiss();
						Intent intent1 = new Intent();
						intent1.setClass(TabMainActivity.this, DeviceScanActivity.class);
						startActivity(intent1);
						TabMainActivity.this.finish();
					}

            	});
            	builder.show();
            }
            
        }
    };
    
    @Override 
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        requestWindowFeature(Window.FEATURE_NO_TITLE);
     //   requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_sports);
		
		final Intent passintent = getIntent();
		mDeviceName = passintent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = passintent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		String steps = passintent.getStringExtra("teststeps1");
		Log.d("debug27","steps:"+steps);
		if( mDeviceName == null || mDeviceAddress ==null) {
			mDeviceAddress = BLUtils.getStringValue(TabMainActivity.this, "bindmacaddr", "");
			mDeviceName = BLUtils.getStringValue(TabMainActivity.this, "bindname", "");
		}
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);  
        if(version >=18) {
        	//initforbleservice();
        }
        intent1 = new Intent(this, BluetoothLeService.class);
        intent1.putExtra("bindmacaddr", mDeviceAddress);
        intent1.putExtra("bindname", mDeviceName);
        this.startService(intent1 );
		button = (ImageView)findViewById(R.id.data);
		sync = (ImageView)findViewById(R.id.sync);
		//text = (TextView)findViewById(R.id.text);
		data_linearlayout = (LinearLayout)findViewById(R.id.data_linearlayout);
		sync_linearlayout = (LinearLayout)findViewById(R.id.sync_linearlayout);
		LayoutInflater flater = LayoutInflater.from(this);
		mPopupWindowView = flater.inflate(R.layout.mypopup, null);
		action_daydata = (TextView)mPopupWindowView.findViewById(R.id.action_daydata);
		action_weekdata = (TextView)mPopupWindowView.findViewById(R.id.action_weekdata);
		action_mouthdata = (TextView)mPopupWindowView.findViewById(R.id.action_mouthdata);
		popupWindow = new PopupWindow(mPopupWindowView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.black));
		
		
    	
		
		
		
        Resources res = getResources(); // Resource object to get Drawables  
        final TabHost tabHost = getTabHost();  // The activity TabHost  
        tabHost.setup(getLocalActivityManager());
        TabWidget tabwidget = tabHost.getTabWidget();
        TabSpec spec;  
        Intent intent;  // Reusable Intent for each tab  
        final View  tab1 = (View)LayoutInflater.from(this).inflate(R.layout.tabmini,null);
        final ImageView tabimage1 = (ImageView)tab1.findViewById(R.id.tabimage);
        final TextView tabtext1 = (TextView)tab1.findViewById(R.id.tabtext);
        tabimage1.setBackgroundResource(R.drawable.sportdata_f);
        tabtext1.setTextColor(Color.parseColor("#0078ff"));
        tabtext1.setText(R.string.sportdata);
      //绗竴涓猅ab  
        intent = new Intent(this,SportsActivity.class);//鏂板缓涓�涓狪ntent鐢ㄤ綔Tab1鏄剧ず鐨勫唴瀹�  
        spec = tabHost.newTabSpec("tab1")//鏂板缓涓�涓� Tab  
        .setIndicator(tab1)//璁剧疆鍚嶇О浠ュ強鍥炬爣  
        .setContent(intent);//璁剧疆鏄剧ず鐨刬ntent锛岃繖閲岀殑鍙傛暟涔熷彲浠ユ槸R.id.xxx  
        tabHost.addTab(spec);//娣诲姞杩泃abHost  
   
        View  tab2 = (View)LayoutInflater.from(this).inflate(R.layout.tabmini,null);
        final ImageView tabimage2 = (ImageView)tab2.findViewById(R.id.tabimage);
        final TextView tabtext2 = (TextView)tab2.findViewById(R.id.tabtext);
        tabimage2.setBackgroundResource(R.drawable.sleepdata);
        tabtext2.setText(R.string.sleepdata);
        //绗簩涓猅ab  
        intent = new Intent(this,SleepActivity.class);//绗簩涓狪ntent鐢ㄤ綔Tab1鏄剧ず鐨勫唴瀹�  
        spec = tabHost.newTabSpec("tab2")//鏂板缓涓�涓� Tab  
        .setIndicator(tab2)//璁剧疆鍚嶇О浠ュ強鍥炬爣  
        .setContent(intent);//璁剧疆鏄剧ず鐨刬ntent锛岃繖閲岀殑鍙傛暟涔熷彲浠ユ槸R.id.xxx  
        tabHost.addTab(spec);//娣诲姞杩泃abHost  
          
        View  tab3 = (View)LayoutInflater.from(this).inflate(R.layout.tabmini,null);
        final ImageView tabimage3 = (ImageView)tab3.findViewById(R.id.tabimage);
        final TextView tabtext3 = (TextView)tab3.findViewById(R.id.tabtext);
        tabimage3.setBackgroundResource(R.drawable.setting);
        tabtext3.setText(R.string.personal_title);
       //绗笁涓猅ab  
        intent = new Intent(this,SettingActivity.class);//绗簩涓狪ntent鐢ㄤ綔Tab1鏄剧ず鐨勫唴瀹�  
        spec = tabHost.newTabSpec("tab3")//鏂板缓涓�涓� Tab  
        .setIndicator(tab3)//璁剧疆鍚嶇О浠ュ強鍥炬爣  
        .setContent(intent);//璁剧疆鏄剧ず鐨刬ntent锛岃繖閲岀殑鍙傛暟涔熷彲浠ユ槸R.id.xxx  
        tabHost.addTab(spec);//娣诲姞杩泃abHost  
        
        View  tab4 = (View)LayoutInflater.from(this).inflate(R.layout.tabmini,null);
        final ImageView tabimage4 = (ImageView)tab4.findViewById(R.id.tabimage);
        final TextView tabtext4 = (TextView)tab4.findViewById(R.id.tabtext);
        tabimage4.setBackgroundResource(R.drawable.about);
        tabtext4.setText(R.string.about_title);
      //绗洓涓猅ab  
        intent = new Intent(this,AboutActivity.class);//绗簩涓狪ntent鐢ㄤ綔Tab1鏄剧ず鐨勫唴瀹�  
        spec = tabHost.newTabSpec("tab4")//鏂板缓涓�涓� Tab  
        .setIndicator(tab4)//璁剧疆鍚嶇О浠ュ強鍥炬爣  
        .setContent(intent);//璁剧疆鏄剧ず鐨刬ntent锛岃繖閲岀殑鍙傛暟涔熷彲浠ユ槸R.id.xxx  
        tabHost.addTab(spec);//娣诲姞杩泃abHost  

        tabHost.setCurrentTab(0);//璁剧疆褰撳墠鐨勯�夐」鍗�,杩欓噷涓篢ab1 
        View view = tabHost.getTabWidget().getChildAt(0); 
//        ImageView iv = (ImageView)view.findViewById(android.R.id.icon);
//        iv.setImageDrawable(getResources().getDrawable(R.drawable.sportdata_f));
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String arg0) {
				tabHost.setCurrentTabByTag(arg0);
				//updateTab(tabHost,tabimage1,tabimage2,tabimage3,tabimage4);
				for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) { 
//		            View view = tabHost.getTabWidget().getChildAt(i); 
//		            ImageView iv = (ImageView)view.findViewById(android.R.id.icon);
//		            TextView tv = (TextView)view.findViewById(android.R.id.title);
		            if (tabHost.getCurrentTab() == i) {//閫変腑  
		            	switch (i){
		            	case 0:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sportdata_f));
		            		Log.d("debug_tab","---sportdata_f---");
		            		tabimage1.setBackgroundResource(R.drawable.sportdata_f);
		            		tabtext1.setTextColor(Color.parseColor("#0078ff"));
		            		break;
		            	case 1:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sleepdata_f));
		            		Log.d("debug_tab","---sleepdata_f---");
		            		tabimage2.setBackgroundResource(R.drawable.sleepdata_f);
		            		tabtext2.setTextColor(Color.parseColor("#0078ff"));
		            		break;
		            	case 2:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.setting_f));
		            		Log.d("debug_tab","---setting_f---");
		            		tabimage3.setBackgroundResource(R.drawable.setting_f);
		            		tabtext3.setTextColor(Color.parseColor("#0078ff"));
		            		break;
		            	case 3:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.about_f));
		            		Log.d("debug_tab","---about_f---");
		            		tabimage4.setBackgroundResource(R.drawable.about_f);
		            		tabtext4.setTextColor(Color.parseColor("#0078ff"));
		            		break;
		            	}
		                 
		            } else {//涓嶉�変腑  
		            	switch (i){
		            	case 0:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sportdata));
		            		Log.d("debug_tab","---sportdata---");
		            		tabimage1.setBackgroundResource(R.drawable.sportdata);
		            		tabtext1.setTextColor(Color.parseColor("#000000"));
		            		break;
		            	case 1:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sleepdata));
		            		Log.d("debug_tab","---sleepdata---");
		            		tabimage2.setBackgroundResource(R.drawable.sleepdata);
		            		tabtext2.setTextColor(Color.parseColor("#000000"));
		            		break;
		            	case 2:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.setting));
		            		Log.d("debug_tab","---setting---");
		            		tabimage3.setBackgroundResource(R.drawable.setting);
		            		tabtext3.setTextColor(Color.parseColor("#000000"));
		            		break;
		            	case 3:
//		            		iv.setImageDrawable(getResources().getDrawable(R.drawable.about)); 
		            		Log.d("debug_tab","---about---");
		            		tabimage4.setBackgroundResource(R.drawable.about);
		            		tabtext4.setTextColor(Color.parseColor("#000000"));
		            		break;
		            	}
		            } 
		        } 
			}
		});
        
        if(mServiceConnection != null) {
        	Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
        handler.sendEmptyMessageDelayed(BEGIN_SYNC_TIMER, 20*1000);
        mLoadingDialog = showLoadingDialog(TabMainActivity.this,""/*TabMainActivity.this.getResources().getString(R.string.syncing)*/);
//        Toast.makeText(
//        		TabMainActivity.this, getResources().getString(R.string.syncing_now), 
//        		Toast.LENGTH_SHORT)
//        	.show();
    }   
    /**
     * 鏇存柊Tab鏍囩鐨勯鑹诧紝鍜屽瓧浣撶殑棰滆壊
     * @param tabHost
     */ 
//    private void updateTab(final TabHost tabHost,ImageView tabimage1,ImageView tabimage2,ImageView tabimage3,ImageView tabimage4) { 
//        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) { 
////            View view = tabHost.getTabWidget().getChildAt(i); 
////            ImageView iv = (ImageView)view.findViewById(android.R.id.icon);
////            TextView tv = (TextView)view.findViewById(android.R.id.title);
//            if (tabHost.getCurrentTab() == i) {//閫変腑  
//            	switch (i){
//            	case 0:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sportdata_f));
//            		Log.d("debug_tab","---sportdata_f---");
//            		tabimage1.setBackgroundResource(R.drawable.sportdata_f);
//            		tabtext1.setTextColor(Color.parseColor("#110183"));
//            		break;
//            	case 1:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sleepdata_f));
//            		Log.d("debug_tab","---sleepdata_f---");
//            		tabimage2.setBackgroundResource(R.drawable.sleepdata_f);
//            		break;
//            	case 2:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.setting_f));
//            		Log.d("debug_tab","---setting_f---");
//            		tabimage3.setBackgroundResource(R.drawable.setting_f);
//            		break;
//            	case 3:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.about_f));
//            		Log.d("debug_tab","---about_f---");
//            		tabimage4.setBackgroundResource(R.drawable.about_f);
//            		break;
//            	}
//                 
//            } else {//涓嶉�変腑  
//            	switch (i){
//            	case 0:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sportdata));
//            		Log.d("debug_tab","---sportdata---");
//            		tabimage1.setBackgroundResource(R.drawable.sportdata);
//            		break;
//            	case 1:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.sleepdata));
//            		Log.d("debug_tab","---sleepdata---");
//            		tabimage2.setBackgroundResource(R.drawable.sleepdata);
//            		break;
//            	case 2:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.setting));
//            		Log.d("debug_tab","---setting---");
//            		tabimage3.setBackgroundResource(R.drawable.setting);
//            		break;
//            	case 3:
////            		iv.setImageDrawable(getResources().getDrawable(R.drawable.about)); 
//            		Log.d("debug_tab","---about---");
//            		tabimage4.setBackgroundResource(R.drawable.about);
//            		break;
//            	}
//            } 
//        } 
//    } 
	
	public Handler handler = new Handler(){
		public void handleMessage(Message msg){
			Bundle bundle = msg.getData();
			switch (msg.what){
			case BEGIN_SYNC_TIMER:
					closeLoadingDialog(TabMainActivity.this,mLoadingDialog);
					Toast.makeText(
	                		TabMainActivity.this, getResources().getString(R.string.sync_failed), 
	                		Toast.LENGTH_SHORT)
	                	.show();
					Log.d("debug09","---BEGIN_SYNC_TIMER---");
//					Intent intent = new Intent();
//					intent.setClass(TabMainActivity.this, DeviceScanActivity.class);
//					startActivity(intent);
//					TabMainActivity.this.finish();
					break;
			case SYNC_OK:
//					BLUtils.setStringValue(TabMainActivity.this, "bindmacaddr", mDeviceAddress);
//					BLUtils.setStringValue(TabMainActivity.this, "bindname", mDeviceName);
					closeLoadingDialog(TabMainActivity.this,mLoadingDialog);
					Toast.makeText(TabMainActivity.this, getResources().getString(R.string.sync_ok), 
							Toast.LENGTH_LONG).show();
					break;
			case BEGIN_RESYNC_TIMER:
					closeLoadingDialog(TabMainActivity.this,mLoadingDialog);
					Toast.makeText(
                		TabMainActivity.this, getResources().getString(R.string.sync_failed), 
                		Toast.LENGTH_SHORT)
                	.show();
					Log.d("debug09","---BEGIN_RESYNC_TIMER---");
					break;
			}
			
		}
	};
	
	private void showPopupWindow(View v){
		Log.d("PopupMenu","popupWindow:"+popupWindow.isShowing());
		if(!popupWindow.isShowing()){
			popupWindow.showAsDropDown(data_linearlayout,35,0);
//			popupWindow.showAtLocation(button, Gravity.LEFT , 10, 10);
		}else {
			popupWindow.dismiss();
		}
	}
	
	
	 private static IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
	        intentFilter.addAction(BluetoothLeService.INDI_ACTION_DATA_AVAILABLE);
	        intentFilter.addAction(Contant.SYNC_OK_INTENT);
	        intentFilter.addAction(Contant.RESYNC_INTENT);
	        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);//去电
	        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");//短信
	        intentFilter.addAction(Contant.TEST_SUCCEEDED);
	        intentFilter.addAction(Contant.TEST_FAILED);
	        
	        //
	        return intentFilter;
	    }
	    
	    private boolean sendCMDResponse( byte cmd_version, byte cmd_type, short res, int seq) {
			byte cmd[] = new byte[4];
			cmd[0] = cmd_version;
			cmd[1] = cmd_type;
			
			// result
			cmd[2] = (byte)((res >> 8)&0xff);
			cmd[3] = (byte)((res >> 0)&0xff);
			
			Device.SendDataToManufacturerSvrResponse  respose =  
					   Utils.buildForFacBuffer(cmd);
			byte[] result = respose.toByteArray();
			
			
			
			byte[] merged = Utils.mergeFromFac(result,
					Device.EmCmdId.ECI_resp_sendDataToManufacturerSvr_VALUE,
					seq);
			String resultHex = Utils.bytesToHexString(merged);
			
			Log.d("BluetoothLeService",
					"resultHex " +resultHex);
			
			if(mWriteCharacteristic != null){
			Log.d("BluetoothLeService",
					"write step response to watch ");
			mBluetoothLeService.writeRXCharacteristic(mWriteCharacteristic,
					merged);
		}
			return true;
	    }
	    //
	    
	    @SuppressLint("NewApi") private void displayGattServices(List<BluetoothGattService> gattServices) {
	        if (gattServices == null) return;
	        String uuid = null;
	       // String unknownServiceString = getResources().getString(R.string.unknown_service);
	       // String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
	        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
	        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
	                = new ArrayList<ArrayList<HashMap<String, String>>>();
	        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	        // Loops through available GATT Services.
	        for (BluetoothGattService gattService : gattServices) {
	            HashMap<String, String> currentServiceData = new HashMap<String, String>();
	            uuid = gattService.getUuid().toString();
	          //  currentServiceData.put(
	            //        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
	           // currentServiceData.put(LIST_UUID, uuid);
	           // gattServiceData.add(currentServiceData);

	            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
	                    new ArrayList<HashMap<String, String>>();
	            List<BluetoothGattCharacteristic> gattCharacteristics =
	                    gattService.getCharacteristics();
	            ArrayList<BluetoothGattCharacteristic> charas =
	                    new ArrayList<BluetoothGattCharacteristic>();

	            // Loops through available Characteristics.
	            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
	                charas.add(gattCharacteristic);
	                HashMap<String, String> currentCharaData = new HashMap<String, String>();
	                uuid = gattCharacteristic.getUuid().toString();
	                Log.d("BluetoothLeService"," Characteristic-"+uuid);
	                if(uuid.equalsIgnoreCase("0000fec8-0000-1000-8000-00805f9b34fb")){
	                	 Log.d("BluetoothLeService"," meet notify character ");
	                	 mNotifyCharacteristic = gattCharacteristic;
	                } 
	                if(uuid.equalsIgnoreCase("0000fec7-0000-1000-8000-00805f9b34fb")){
	                	mWriteCharacteristic = gattCharacteristic;
	                	Log.d("BluetoothLeService"," meet write character ");
	                }
	               // currentCharaData.put(
	                //        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
	              //  currentCharaData.put(LIST_UUID, uuid);
	              //  gattCharacteristicGroupData.add(currentCharaData);
	            }
	            mGattCharacteristics.add(charas);
	            gattCharacteristicData.add(gattCharacteristicGroupData);
	        }
	    }
	    
	    
	    @Override
	    protected void onResume() {
	        super.onResume();
	        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	        /*if (mBluetoothLeService != null) {
	            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
	            Log.d(TAG, "Connect request result=" + result);
	        }*/
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        unregisterReceiver(mGattUpdateReceiver);
	        /*if (mBluetoothLeService != null) {
	            mBluetoothLeService.disconnect();
	            Log.d(TAG, "onPause: disconnect device" );
	        }*/
	    }
	    
	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        if (mBluetoothLeService != null) {
	            mBluetoothLeService.disconnect();
	            unbindService(mServiceConnection);
		        mBluetoothLeService = null;
	            Log.d(TAG, "onPause: disconnect device" );
	        }
//	        unbindService(mServiceConnection);
//	        mBluetoothLeService = null;
	    }
	    
	    public static Dialog showLoadingDialog(Context context,String text){
	        Dialog LoadingDialog;

			LoadingDialog = createLoadingDialog(context,text);
			LoadingDialog.show();
			return LoadingDialog;
//			mHandler.sendEmptyMessageAtTime(LOGING_CONNECT_TIMEOUT, 1000*60);
		}
		static void closeLoadingDialog(Context context,Dialog dialog){
			if(!((Activity) context).isFinishing()){
				dialog.dismiss();
			}
			
		}
		public static Dialog createLoadingDialog(Context context, String msg) {  
			  
		        LayoutInflater inflater = LayoutInflater.from(context);  
		        View v = inflater.inflate(R.layout.loading_dialog, null);// 寰楀埌鍔犺浇view  
		        FrameLayout layout = (FrameLayout) v.findViewById(R.id.dialog_view);// 鍔犺浇甯冨眬  
		        // main.xml涓殑ImageView  
		        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);  
		        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 鎻愮ず鏂囧瓧  
		        // 鍔犺浇鍔ㄧ敾  
		        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
		                context, R.anim.loading_animation);  
		        // 浣跨敤ImageView鏄剧ず鍔ㄧ敾  
		        spaceshipImage.startAnimation(hyperspaceJumpAnimation);  
		        tipTextView.setText(msg);// 璁剧疆鍔犺浇淇℃伅  
		  
		        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 鍒涘缓鑷畾涔夋牱寮廳ialog  
		  
		        loadingDialog.setCancelable(true);// 鍙互鐢ㄢ�滆繑鍥為敭鈥濆彇娑�  
		        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(  
		                LinearLayout.LayoutParams.FILL_PARENT,  
		                LinearLayout.LayoutParams.FILL_PARENT));// 璁剧疆甯冨眬  
		        return loadingDialog;  
		  
		    }  
} 
