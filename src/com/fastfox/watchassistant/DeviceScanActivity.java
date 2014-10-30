/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fastfox.watchassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;
import com.excheer.network.ListenNetStateService;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private class  DeviceInfo {
    	BluetoothDevice device;
    	String devicespec;
    }
    private String mBindMacAddress;
    private String mBindName;
    @SuppressLint("NewApi") @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //startService(new Intent(this, ListenNetStateService.class));
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        new CheckUpdateTask(this).execute();
        Drawable drawable = getResources().getDrawable(R.drawable.white);
        this.getWindow().setBackgroundDrawable(drawable);
        //getActionBar().setTitle(R.string.title_devices);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        int version = Integer.valueOf(android.os.Build.VERSION.SDK);  
        if(version <18) {
        	  final Intent intent = new Intent(this, TabMainActivity.class);
              intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, "");
              intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, "");
              
              startActivity(intent);
              finish();
        	return ;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        initLeCallback();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mHandler = new Handler();
        mBindMacAddress = BLUtils.getStringValue(DeviceScanActivity.this, "bindmacaddr", "");
        mBindName = BLUtils.getStringValue(DeviceScanActivity.this, "bindname", "");
        /*int num = 45;
       
        int stepCount = StepFacade.getStepCount(DeviceScanActivity.this);
        Log.d("test","stepCount "+stepCount);
        ArrayList<Steps> hhh = StepFacade.getAllStepsList(DeviceScanActivity.this);
        for(int i=0;i<hhh.size();i++){
        	Steps s = hhh.get(i);
        	Log.d("test","step  starttime"+s.getStartTime() +" end "+s.getEndTime()+" step "+s.getSteps());
        }
       */
        
       
    }

  

    @SuppressLint("NewApi") @Override
    protected void onResume() {
        super.onResume();

        if(mBindMacAddress == null || mBindMacAddress.isEmpty()) {
        	// do nothing!!
        } else {
        	final Intent intent = new Intent(this, TabMainActivity.class);
            intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, mBindName);
            intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mBindMacAddress);
            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            startActivity(intent);
            
            DeviceScanActivity.this.finish();
            return;
        }
        
        
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        
        setListAdapter(mLeDeviceListAdapter);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_sports);
        LinearLayout data_linearlayout = (LinearLayout)findViewById(R.id.data_linearlayout);
        LinearLayout sync_linearlayout = (LinearLayout)findViewById(R.id.sync_linearlayout);
        data_linearlayout.setVisibility(View.GONE);
			sync_linearlayout.setVisibility(View.GONE);
        TextView titleView = (TextView)findViewById(R.id.title);
        titleView.setText(R.string.devices_list); 
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        if(mLeDeviceListAdapter != null) {
        	mLeDeviceListAdapter.clear();
        }
        
    }

    @SuppressLint("NewApi") @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	final DeviceInfo device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        
        final Intent intent = new Intent(this, TabMainActivity.class);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.device.getName());
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
        finish();
    }

    @SuppressLint("NewApi") private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<DeviceInfo> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<DeviceInfo>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(DeviceInfo device) {
        	
        	boolean find = false;
        	for(int i = 0; i<mLeDevices.size();i++){
        		DeviceInfo item = mLeDevices.get(i);
        		if(item.devicespec.equalsIgnoreCase(device.devicespec)){
        			find = true;
        			break;
        		}
        	}
        	if(!find) {
        		 if(!mLeDevices.contains(device)) {
                     mLeDevices.add(device);
                 }
        	}
           
        }

        public DeviceInfo getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceSpecific = (TextView)view.findViewById(R.id.device_specific);
                
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            DeviceInfo device = mLeDevices.get(i);
            final String deviceName = device.device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.device.getAddress());
            if(device.devicespec!= null){
            	String d = "" ; 
            	//for(int j =0;j<device.devicespec.length;j++){
            	//	d += device.devicespec[j]+" ";
            	//}
            	viewHolder.deviceSpecific.setText(device.devicespec);
            }
            return view;
        }
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;
    // Device scan callback.
    @SuppressLint("NewApi")  private void initLeCallback(){
    	mLeScanCallback =
                new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	DeviceInfo info = new DeviceInfo();
                    	info.device = device;
                    	//if(scanRecord != null && scanRecord.length>25)
                    	{
                    		//info.devicespec = new byte[scanRecord.length];
                    		//if(scanRecord[0] == 0x08 &&
                    			//	scanRecord[1] == 0x09)
                    		if(/*device.getName().equalsIgnoreCase("FastFox-Lite")*/true)
                    		{
                    			byte[] newRecord = new byte[6];
                    			for(int i=0;i<6;i++){
                    				newRecord[i] = scanRecord[i+16];
                    			}
                    			info.devicespec  = bytesToHexString( newRecord);
                                mLeDeviceListAdapter.addDevice(info);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                    		}
                    		
                    	}
                    	
                    	
                    }
                });
            }
        };
    }
    
   //private BluetoothAdapter.LeScanCallback 
    private static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {   
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }  
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceSpecific;
    }
    
    
}