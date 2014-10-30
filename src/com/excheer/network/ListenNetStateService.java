package com.excheer.network;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.bluefay.core.BLHttp;
import com.fastfox.watchassistant.Contant;
import com.fastfox.watchassistant.StepFacade;
import com.fastfox.watchassistant.Steps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class ListenNetStateService extends Service {
	private static final String TAG = "ListenNetStateService";
    private ConnectivityManager connectivityManager;
    private NetworkInfo info;
    private static boolean incomingFlag = false;
    private static String incoming_number = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d("network", "---network change---");
                connectivityManager = (ConnectivityManager)      
                                         getSystemService(Context.CONNECTIVITY_SERVICE);
                info = connectivityManager.getActiveNetworkInfo();  
                if(info != null && info.isAvailable()) {
                    String name = info.getTypeName();
                    Log.d("network", "network_name:" + name);
                    Toast.makeText(context,name,Toast.LENGTH_SHORT).show();
                    //upload data
                    String uploadurl = Contant.UPLOAD_STEPS;
                    ArrayList<Steps> stepList = StepFacade.getAllStepsList(context);
                    Log.d("network","stepList_size:"+stepList.size());
                    for(int i=0;i<stepList.size();i++) {
						Steps s = stepList.get(i);
	                    byte[] bytedata;
						try {
							bytedata = objectToBytes(s);
							Log.d("network","bytedata:"+bytedata);
							byte[] res = BLHttp.post(uploadurl, bytedata);
							String str = new String(res);
//							String str = new String(res,"UTF-8");
							Log.d("network","res:"+str);
						} catch (Exception e) {
							e.printStackTrace();
						}
	                    
                    }
                } else {
                	Toast.makeText(context,"network disconnect",Toast.LENGTH_SHORT).show();
                    Log.d("network", "---no network---");
                }
            }
            //如果是拨打电话
            if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){                        
                    incomingFlag = false;
                    String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Toast.makeText(context, "call OUT:"+phoneNumber, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "call OUT:"+phoneNumber);                        
            }else{                        
                    //如果是来电
                    TelephonyManager tm = 
                        (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);                        
                    
                    switch (tm.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING://来电
                            incomingFlag = true;//标识当前是来电
                            incoming_number = intent.getStringExtra("incoming_number");
                            Toast.makeText(context, "RINGING :"+ incoming_number, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "RINGING :"+ incoming_number);
                            break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://摘机（正在通话中）                                
                            if(incomingFlag){
                            		Toast.makeText(context, "incoming ACCEPT :"+ incoming_number, Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "incoming ACCEPT :"+ incoming_number);
                            }
                            break;
                    
                    case TelephonyManager.CALL_STATE_IDLE://空闲                                
                            if(incomingFlag){
                            		Toast.makeText(context, "incoming IDLE", Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "incoming IDLE");                                
                            }
                            break;
                    } 
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);//来电
        mFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);//去电
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 对象转Byte数组
     */
    public static byte[] objectToBytes(Object obj) throws Exception{
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	ObjectOutputStream sout = new ObjectOutputStream(out);
    	Log.d("network","---objectToBytes---");
    	sout.writeObject(obj);
    	sout.flush();
    	
    	byte[] bytes = out.toByteArray();
    	Log.d("network","---objectToBytes---"+bytes);
    	return bytes;
    }
}
