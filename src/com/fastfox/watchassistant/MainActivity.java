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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.fastfox.watchtest.R;
import com.excheer.protobuf.Device;
import com.excheer.protobuf.Device.BaseResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // excheer protocol
    public final static byte EXCHEER_PROTOCOL_VERSION = 0x00;
    
    public static final short CMD_RES_OK = 0;
    
    public final static int EXCHEER_CmdId__VERSION = 1;
    public final static int EXCHEER_CmdId__BATTERY = 2;
    public final static int EXCHEER_CmdId__TIME =  3;
    public final static int	EXCHEER_CmdId__FIRMWARELEN = 4;
    public final static int	EXCHEER_CmdId__STEP = 5;
    public final static int	EXCHEER_CmdId__FIRMWAREREQUEST = 6;
    public final static int	EXCHEER_CmdId__FIRMWARE = 7;
    public final static int EXCHEER_CmdId__REQUESTTIME = 8;
    public final static int EXCHEER_CmdId__LOG = 9;
    public final static int EXCHEER_CmdId__SETTING = 10;
    public final static int EXCHEER_CmdId__DEBUG = 11;
    public final static int EXCHEER_CmdId__DEBUGTOSERVER = 12;
    			
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
    public final static int CACHE_SIZE = 1024;
    byte[] mArray = new byte[CACHE_SIZE];
    private int mRecvCount = 0;
    int mIndex = 0;
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("NewApi") @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if(BluetoothLeService.INDI_ACTION_DATA_AVAILABLE.equals(action)){
            	byte[] array= intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
            	if(array != null && array.length>0){
            		String res = bytesToHexString(array);
            		Log.d("BluetoothLeService","meet indicate extra data "+res);
            	}
            	int copy_count = array.length;
            	if((mRecvCount + array.length) <= CACHE_SIZE) {
            		
            	} else {
            		copy_count = CACHE_SIZE-mRecvCount;
            	}
            	if(copy_count>0){
            	} else {
            		return;
            	}
            	
            	//byte[] array = extra.getBytes();
            	
            	for(int i =0;i<array.length;i++){
            		mArray[i+mRecvCount] =array[i];
            	}
            	mRecvCount += array.length;
            	
            	if(mRecvCount>=8){
            		byte magicNum = mArray[0];
            		byte ver = mArray[1];
            		//int length = (int) ((short)mArray[2]<<8| mArray[3]);
            		short length = (short)(((mArray[2]&0xff)<<8) | ((mArray[3]&0xff)<<0));
            		
            		
            		short cmdId = (short)(((mArray[4]&0xff)<<8) | ((mArray[5]&0xff)<<0));
            		//int cmdId = (int) ((short)mArray[4]<<8| mArray[5]);
            		//int seq = (int) ((short)mArray[6]<<8| mArray[7]);
            		short seq = (short)(((mArray[6]&0xff)<<8) | ((mArray[7]&0xff)<<0));
            		
            		//byte magicNum = mArray[0];
            		Log.d("BluetoothLeService","magic "+ magicNum+
            				" ver "+ver +" len "+length +
            				" id "+cmdId+" seq "+seq);
            		if(mRecvCount >= length /*&& magicNum == 0xfe*/) {
            			if(cmdId == Device.EmCmdId.ECI_req_auth.getNumber()){
                			Log.d("BluetoothLeService","meet auth request");
                			Device.AuthResponse.Builder  builder = Device.AuthResponse.newBuilder();
                			ByteString dd = ByteString.copyFromUtf8("copyFromUtf8");
                			builder.setAesSessionKey(dd);
                			Device.BaseResponse.Builder baseresbuilder = Device.BaseResponse.newBuilder();
                			baseresbuilder.setErrCode(0);
                			baseresbuilder.setErrMsg("success");
                			
                			builder.setBaseResponse(baseresbuilder.build());
                			Device.AuthResponse response = builder.build();
                			
                			byte[] result = response.toByteArray();
                			
                			byte[] merged = new byte[result.length+8];
                			int ccc = 0xfe;
                			int cmd = Device.EmCmdId.ECI_resp_init_VALUE;
                			merged[0]  = (byte)ccc;
                			merged[1] = 1;
                			
                			merged[2] =  (byte)((merged.length>>8)&0xff);
                			merged[3] =  (byte)((merged.length)&0xff);
                			
                			merged[4] =  (byte)((cmd>>8)&0xff);
                			merged[5] =  (byte)((cmd)&0xff);
                			
                			merged[6] =  (byte)((seq>>8)&0xff);
                			merged[7] =  (byte)((seq)&0xff);
                			
                			
                			for(int j=0;j<result.length;j++){
                				merged[8+j] = result[j];
                			}
                			if(mWriteCharacteristic != null){
                				Log.d("BluetoothLeService",
                						"write init response to watch ");
                				mBluetoothLeService.writeRXCharacteristic(mWriteCharacteristic, merged);
                			}
                		} else if(cmdId == Device.EmCmdId.ECI_req_init.getNumber()){
                			Log.d("BluetoothLeService","meet ECI_req_init request");
                			
                			Device.InitResponse.Builder  builder = Device.InitResponse.newBuilder();
                			
                			Device.BaseResponse.Builder baseresbuilder = Device.BaseResponse.newBuilder();
                			baseresbuilder.setErrCode(0);
                			baseresbuilder.setErrMsg("success");
                			builder.setBaseResponse(baseresbuilder.build());
                			
                			Device.InitResponse response = builder.build();
                			
                			byte[] result = response.toByteArray();
                			
                			byte[] merged = new byte[result.length+8];
                			int ccc = 0xfe;
                			int cmd = Device.EmCmdId.ECI_resp_init_VALUE;
                			merged[0]  = (byte)ccc;
                			merged[1] = 1;
                			
                			merged[2] =  (byte)((merged.length>>8)&0xff);
                			merged[3] =  (byte)((merged.length)&0xff);
                			
                			merged[4] =  (byte)((cmd>>8)&0xff);
                			merged[5] =  (byte)((cmd)&0xff);
                			
                			merged[6] =  (byte)((seq>>8)&0xff);
                			merged[7] =  (byte)((seq)&0xff);
                			
                			
                			for(int j=0;j<result.length;j++){
                				merged[8+j] = result[j];
                			}
                			if(mWriteCharacteristic != null){
                				Log.d("BluetoothLeService",
                						"write auth response to watch ");
                				mBluetoothLeService.writeRXCharacteristic(mWriteCharacteristic, merged);
                			}
                		
                			
                		} else if(cmdId == Device.EmCmdId.ECI_req_sendDataToManufacturerSvr.getNumber()){
                			Log.d("BluetoothLeService","meet ECI_req_sendDataToManufacturerSvr request");
                			
                			//Device.SendDataToManufacturerSvrResponse.Builder builder = 
                			//	Device.SendDataToManufacturerSvrResponse.newBuilder();	
                			
                			
                			try {
                    			
                    			byte[] xx = new byte[mRecvCount-8];
                    			for(int i=0;i<mRecvCount-8;i++){
                    				xx[i] = mArray[8+i];
                    			}
                    			Device.SendDataToManufacturerSvrRequest  request = 
                    					Device.SendDataToManufacturerSvrRequest.parseFrom(xx);
                    			ByteString baseString = request.getData();
                    			byte[] facData = baseString.toByteArray();
                    			
                    			Log.d("BluetoothLeService","cmd version "+facData[0]+
                    					" type "+facData[1]);
                    			switch(facData[1]){
                    				case EXCHEER_CmdId__VERSION:
                    				{
                    					short version = (short)(((facData[2]&0xff)<<8) | (facData[3]&0xff));
                    					char model[] = new char[3];
                    					model[0] = (char)facData[4];
                    					model[1] = (char)facData[5];
                    					model[2] = (char)facData[6];
                    					Log.d("BluetoothLeService","version "+version);
                    					sendCMDResponse( EXCHEER_PROTOCOL_VERSION, facData[1], CMD_RES_OK,seq);	
                    				}
                    					break;
                    				case EXCHEER_CmdId__BATTERY:
                    				{
                    					byte bl = facData[2];
                    					Log.d("BluetoothLeService","battery "+bl);
                    					sendCMDResponse( EXCHEER_PROTOCOL_VERSION, facData[1], CMD_RES_OK,seq);	
                    				}
                    					break;
                    				case EXCHEER_CmdId__TIME:
                    					break;
                    				case EXCHEER_CmdId__FIRMWARELEN:
                    					break;
                    				case EXCHEER_CmdId__STEP:
                    					//
                    				{
                    					//short stepCount = (short) ((short)facData[2]<<8| facData[3]);
                    					short blocks = (short)(((facData[2]&0xff)<<8) | ((facData[3]&0xff)<<0));
                    					Log.d("BluetoothLeService",
                    							"stepCount "+blocks);
                    					/*int starttime =  (int) (
                    							(int)facData[4]<<24|
                    							(int)facData[5]<<16|
                    							(int)facData[6]<<8|
                    							facData[7]);*/
                    					long startTime = (long)(((facData[4]&0xff) << 24) | 
                    							((facData[5]&0xff) << 16) | 
                    							((facData[6]&0xff) << 8) | 
                    							((facData[7]&0xff) << 0));
                    					Log.d("BluetoothLeService"," start time "+startTime);
                    					long lastEndTime = 0;
                    					for(int i=0;i<blocks;i++){
                    						int pos = 8 + 6*i;
                    						byte[] endandsteptime = new byte[6];
                    						endandsteptime[0] = facData[i*6+8+0];
                    						endandsteptime[1] = facData[i*6+8+1];
                    						endandsteptime[2] = facData[i*6+8+2];
                    						endandsteptime[3] = facData[i*6+8+3];
                    						endandsteptime[4] = facData[i*6+8+4];
                    						endandsteptime[5] = facData[i*6+8+5];
                    						
                    						String endandsteptimestr = bytesToHexString(endandsteptime);
                    						Log.d("BluetoothLeService"," endandsteptimestr "+endandsteptimestr);
                    						/*int end =  (int) (
                        							(int)facData[i*6+8+0]<<24|
                        							(int)facData[i*6+8+1]<<16|
                        							(int)facData[i*6+8+2]<<8|
                        							(int)facData[i*6+8+3]);*/
                    						long ts = (long)(((facData[pos]&0xff) << 24) | 
                    								((facData[pos + 1]&0xff) << 16) | 
                    								((facData[pos + 2]&0xff) << 8) | 
                    								((facData[pos + 3]&0xff) << 0));
                    						boolean sleep = false;
                    						short steps;
                    						if (((facData[pos + 4]&0xff)>>7) == 1) {
                    							sleep = true;
                    							steps = (short)(((facData[pos + 4] & 0x7F) << 8) | (facData[pos + 5]&0xff));
                    						}
                    						// walking or running data
                    						else {
                    							sleep = false;
                    							steps = (short)(((facData[pos + 4]&0x7f) << 8) | (facData[pos + 5]&0xff));
                    						}
                    						///short step = 
                    						//		(short) ((short)facData[i*6+8+4]<<8| facData[i*6+8+5]);
                    						Log.d("BluetoothLeService"," end "+ts+" step "+steps+" sleep "+sleep);
                    						
                    						if (i == 0) {
                    							StepFacade.addStep(
                    									MainActivity.this, (startTime)*1000, (ts)*1000, 
                    									sleep ? Steps.STEP_TYPE_SLEEPING : Steps.STEP_TYPE_WALKING, 
                    											steps);
                    						} else {
                    							StepFacade.addStep(
                    									MainActivity.this, (lastEndTime)*1000, (ts)*1000, 
                    									sleep ? Steps.STEP_TYPE_SLEEPING : Steps.STEP_TYPE_WALKING,
                    											steps);
                    						}
                    						
                    						
                    						lastEndTime = ts;
                    					}
                    					
                    					///////// send step response
                    					{

                        					byte[] data = new byte[4];
                        					data[0] = EXCHEER_PROTOCOL_VERSION;
                        					data[1] = EXCHEER_CmdId__STEP;
                        					data[2] = 0;
                        					data[3] = 0;
                        					
                                			
                        					Device.SendDataToManufacturerSvrResponse  respose =  
                        					   buildForFacBuffer(data);
                        					byte[] result = respose.toByteArray();
                        					
                        					
                        					
                        					byte[] merged = mergeFromFac(result,
                        							Device.EmCmdId.ECI_resp_sendDataToManufacturerSvr_VALUE,
                        							seq);
                        					String resultHex = bytesToHexString(merged);
                        					
                        					Log.d("BluetoothLeService",
                            						"resultHex " +resultHex);
                        					
                        					if(mWriteCharacteristic != null){
                                				Log.d("BluetoothLeService",
                                						"write step response to watch ");
                                				mBluetoothLeService.writeRXCharacteristic(mWriteCharacteristic,
                                						merged);
                                			}
                    					}
                    				}
                    					break;
                    				case EXCHEER_CmdId__FIRMWAREREQUEST:
                    					break;
                    				case EXCHEER_CmdId__FIRMWARE:
                    					break;
                    				case EXCHEER_CmdId__REQUESTTIME:
                    				{
                    					byte[] data = new byte[7];
                    					data[0] = EXCHEER_PROTOCOL_VERSION;
                    					data[1] = EXCHEER_CmdId__TIME;
                    					//long time = System.currentTimeMillis();
                    					
                    					long now = (new Date().getTime()/1000);
                    					
                    					data[2] = (byte)((now >> 24)&0xff);
                    					data[3] = (byte)((now >> 16)&0xff);
                    					data[4] = (byte)((now >> 8)&0xff);
                    					data[5] = (byte)((now >> 0)&0xff);
                    					
                    					/*int times =(int) (time/1000);
                    					data[2] =  (byte)((times)&0xff);
                    					data[3] =  (byte)((times>>8)&0xff);
                    					data[4] =  (byte)((times>>16)&0xff);
                    					data[5] =  (byte)((times>>24)&0xff);
                    					*/
                    					data[6] = 8;
                            			
                    					Device.SendDataToManufacturerSvrResponse  respose =  
                    					   buildForFacBuffer(data);
                    					
                    					//respose.
                    					byte[] result = respose.toByteArray();
                    					
                    					
                    					
                    					byte[] merged = mergeFromFac(result,
                    							Device.EmCmdId.ECI_resp_sendDataToManufacturerSvr_VALUE,
                    							seq);
                    					String resultHex = bytesToHexString(merged);
                    					
                    					Log.d("BluetoothLeService",
                        						"resultHex " +resultHex);
                    					
                    					if(mWriteCharacteristic != null){
                            				Log.d("BluetoothLeService",
                            						"write fac response to watch ");
                            				mBluetoothLeService.writeRXCharacteristic(mWriteCharacteristic,
                            						merged);
                            			}
                    				}
                    					break;
                    				case EXCHEER_CmdId__LOG:
                    					break;
                    				case EXCHEER_CmdId__SETTING:
                    					break;
                    				case EXCHEER_CmdId__DEBUG:
                    					break;
                    				case EXCHEER_CmdId__DEBUGTOSERVER:
                    					break;
                    			}
                    			//request.get
        					} catch (InvalidProtocolBufferException e) {
        						e.printStackTrace();
        					}
                			
                			
                			/*
                			Device.BaseResponse.Builder baseresbuilder = Device.BaseResponse.newBuilder();
                			baseresbuilder.setErrCode(0);
                			baseresbuilder.setErrMsg("success");
                			builder.setBaseResponse(baseresbuilder.build());
                			
                			Device.SendDataToManufacturerSvrResponse response = builder.build();
                			
                			byte[] result = response.toByteArray();
                			
                			byte[] merged = new byte[result.length+8];
                			int ccc = 0xfe;
                			int cmd = Device.EmCmdId.ECI_resp_sendDataToManufacturerSvr_VALUE;
                			merged[0]  = (byte)ccc;
                			merged[1] = 1;
                			
                			merged[2] =  (byte)((result.length>>8)&0xff);
                			merged[3] =  (byte)((result.length)&0xff);
                			
                			merged[4] =  (byte)((cmd>>8)&0xff);
                			merged[5] =  (byte)((cmd)&0xff);
                			
                			merged[6] =  (byte)((seq>>8)&0xff);
                			merged[7] =  (byte)((seq)&0xff);
                			
                			
                			for(int j=0;j<result.length;j++){
                				merged[8+j] = result[j];
                			}
                			if(mWriteCharacteristic != null){
                				Log.d("BluetoothLeService",
                						"write auth response to watch ");
                				mBluetoothLeService.writeRXCharacteristic(mWriteCharacteristic, merged);
                			}
                			*/
                		} else {
                			Log.d("BluetoothLeService","meet unknown  request "+cmdId);
                		}
            			
            			mRecvCount = 0;
            		}
            		
            	}
            	
            /*	mIndex += array.length;
            	if(mCount == 37) {
            		try {
            			
            			byte[] xx = new byte[mCount-8];
            			for(int i=0;i<mCount-8;i++){
            				xx[i] = mArray[8+i];
            			}
            			AuthRequest request = AuthRequest.parseFrom(xx);
            			Log.d("BluetoothLeService","proto version "+request.getProtoVersion()+
            					" device name "+request.getDeviceName()+" auth "+request.getAuthMethod());
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
					}
            		Log.d("BluetoothLeService","get total auth request packet");
            	}*/
            	
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @SuppressLint("NewApi") @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        /*
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                          
                            if (mNotifyCharacteristic != null) {
                            	 Log.d("BluetoothLeService"," listen  PROPERTY_READ!!! ");
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }*/
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            Log.d("BluetoothLeService"," listen  notify!!! ");
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                            
                          //  BluetoothGattDescriptor descriptor = mNotifyCharacteristic.getDescriptor(CCCD);
                          //  descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                          //  mBluetoothGatt.writeDescriptor(descriptor);
                           // mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        //mBluetoothLeService.get
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @SuppressLint("NewApi") @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    @SuppressLint("NewApi") private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

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
                	 
                } 
                if(uuid.equalsIgnoreCase("0000fec7-0000-1000-8000-00805f9b34fb")){
                	mWriteCharacteristic = gattCharacteristic;
                	Log.d("BluetoothLeService"," meet write character ");
                }
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.INDI_ACTION_DATA_AVAILABLE);
        
        //
        return intentFilter;
    }
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
    
    byte[] mergeFromFac(byte[] facdata, int cmd, int seq){
    	byte[] merged = new byte[facdata.length+8];
		int ccc = 0xfe;
		//int cmd = Device.EmCmdId.ECI_resp_init_VALUE;
		merged[0]  = (byte)ccc;
		merged[1] = 1;
		
		merged[2] =  (byte)((merged.length>>8)&0xff);
		merged[3] =  (byte)((merged.length)&0xff);
		
		merged[4] =  (byte)((cmd>>8)&0xff);
		merged[5] =  (byte)((cmd)&0xff);
		
		merged[6] =  (byte)((seq>>8)&0xff);
		merged[7] =  (byte)((seq)&0xff);
		
		
		for(int j=0;j<facdata.length;j++){
			merged[8+j] = facdata[j];
		}
		return merged;
    }
    Device.SendDataToManufacturerSvrResponse buildForFacBuffer(byte[] facBuffer){
    	Device.SendDataToManufacturerSvrResponse.Builder builder = 
				Device.SendDataToManufacturerSvrResponse.newBuilder();	
    	Device.BaseResponse.Builder baseresbuilder = Device.BaseResponse.newBuilder();
		baseresbuilder.setErrCode(0);
		baseresbuilder.setErrMsg("s");
		
		builder.setBaseResponse(baseresbuilder.build());
		//builder.setData(value)
		ByteString byteString = ByteString.copyFrom(facBuffer);
    	builder.setData(byteString);
    	
    	//builder.
    	Device.SendDataToManufacturerSvrResponse res = builder.build();
    	
    	Log.d("BluetoothLeService"," HAS DATA  "+res.hasData()+" byteString "+byteString);
    	return res;
    }
    
    private boolean sendCMDResponse( byte cmd_version, byte cmd_type, short res, int seq) {
		byte cmd[] = new byte[4];
		cmd[0] = cmd_version;
		cmd[1] = cmd_type;
		
		// result
		cmd[2] = (byte)((res >> 8)&0xff);
		cmd[3] = (byte)((res >> 0)&0xff);
		
		Device.SendDataToManufacturerSvrResponse  respose =  
				   buildForFacBuffer(cmd);
		byte[] result = respose.toByteArray();
		
		
		
		byte[] merged = mergeFromFac(result,
				Device.EmCmdId.ECI_resp_sendDataToManufacturerSvr_VALUE,
				seq);
		String resultHex = bytesToHexString(merged);
		
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
		//return writeResponseDeviceMsg(msg, cmd);
		//return sendDeviceMessage(msg.getDeviceType(), msg.getDeviceId(), msg.getOpenId(), cmd);
	
    
}
