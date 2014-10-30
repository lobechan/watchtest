

package com.fastfox.watchassistant;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.bluefay.android.BLUtils;
import com.fastfox.watchtest.R;
import com.excheer.protobuf.Device;
import com.excheer.until.Utils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private String mBluetoothDeviceName;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int BLUETOOTHLESEVICE_DISCONNECT = 10001;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    
    public final static String INDI_ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.INDI_ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
   
    private static boolean incomingFlag = false;
    private static boolean smscomingFlag = false;
    private static String incoming_number = null;
    private static int ledcorlor = 1;
    private static int cycle_max = 3;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    @SuppressLint("NewApi") private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        	Log.w(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
        	 Log.w(TAG, "onCharacteristicRead received: " );
       
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	Log.w(TAG, "onCharacteristicChanged received: " );
            
        	broadcastUpdateIndicate(INDI_ACTION_DATA_AVAILABLE, characteristic);
        }
        boolean init = false;
        @Override
        public  void  onCharacteristicWrite (BluetoothGatt gatt,   BluetoothGattCharacteristic characteristic, 
        		int  status){
        	Log.w(TAG, "onCharacteristicWrite : "+ characteristic+ " status "+status);
        	//if(!init) 
        	{
        		init = true;
        		/**
        		 * if(cacheCount > 20){
    		sendIndex = 20;
    	} else {
    		sendIndex =cacheCount;
    	}
        		 */
        		if(sendIndex < cacheCount){
        			
        			if((cacheCount-sendIndex)>MAX_BLE_LEN) {
        				
        				byte[] added = new byte[MAX_BLE_LEN];
                    	for(int i=0;i<added.length;i++){
                    		added[i] = gvalue[sendIndex+i];
                    	}
                    	sendIndex += (MAX_BLE_LEN);
                    	characteristic.setValue(added);
                    	boolean status1 = mBluetoothGatt.writeCharacteristic(characteristic);
                    	Log.w(TAG, "onCharacteristicWrite : new write res "+status1+" len "+added.length);
                    	
        			} else {
        				byte[] added = new byte[cacheCount-sendIndex];
                    	for(int i=0;i<added.length;i++){
                    		added[i] = gvalue[sendIndex+i];
                    	}
                    	sendIndex += (cacheCount-sendIndex);
                    	characteristic.setValue(added);
                    	boolean status1 = mBluetoothGatt.writeCharacteristic(characteristic);
                    	Log.w(TAG, "onCharacteristicWrite : new write res "+status1+" len "+added.length);
        			}
        			
        		} else {
        			cacheCount = 0;
        			sendIndex = 0;
        			Log.w(TAG, "onCharacteristicWrite :  finish send buffer");
        		}
        		
        	}
        	
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void broadcastUpdateIndicate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    
    @SuppressLint("NewApi") private void broadcastUpdateIndicate(final String action,
            final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		
		// This is special handling for the Heart Rate Measurement profile.  Data parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
				Log.d(TAG, "Heart rate format UINT16.");
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
				Log.d(TAG, "Heart rate format UINT8.");
			}
			final int heartRate = characteristic.getIntValue(format, 1);
			Log.d(TAG, String.format("Received heart rate: %d", heartRate));
			intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
		} else {
			// For all other profiles, writes the data formatted in HEX.
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				intent.putExtra(EXTRA_DATA, data);
				
			}
		}
		sendBroadcast(intent);
}
    
    
    @SuppressLint("NewApi") private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    @SuppressLint("NewApi") public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    @SuppressLint("NewApi") public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
            	Log.d(TAG, "connect exist ok.");
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
            	Log.d(TAG, "connect exist failed.");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        Log.d("debug27","mBluetoothDeviceAddress:"+mBluetoothDeviceAddress);
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    @SuppressLint("NewApi") public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    @SuppressLint("NewApi") public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
       
       boolean res =  mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
       Log.w(TAG, "setCharacteristicNotification res "+res+" enabled "+enabled);
        // This is specific to Heart Rate Measurement.
       if (true/*UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())*/) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    @SuppressLint("NewApi") public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    
    private final static int MAX_BLE_LEN = 20;
    
    byte[] gvalue = new byte[512];
    int cacheCount = 0;
    int sendIndex = 0;
  //byte added[] = new 
    // add for excheer
    @SuppressLint("NewApi") public void writeRXCharacteristic(BluetoothGattCharacteristic characteristic,
    				byte[] value)
    {
    	cacheCount = value.length;
    	sendIndex = 0;
    	for(int i=0;i<value.length;i++){
    		gvalue[i] = value[i];
    	}
    	
    	if(cacheCount > MAX_BLE_LEN){
    		sendIndex = MAX_BLE_LEN;
    	} else {
    		sendIndex =cacheCount;
    	}
    	
    	byte[] added = new byte[sendIndex];
    	for(int i=0;i<added.length;i++){
    		added[i] = gvalue[i];
    	}
    	Log.d(TAG, "write TXchar - len " + value.length);  
        characteristic.setValue(added);
    	boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
    	Log.d(TAG," status "+status+" characteristic "+characteristic);
    }
    
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    
    public final static int CACHE_SIZE = 1024;
    byte[] mArray = new byte[CACHE_SIZE];
    private int mRecvCount = 0;
    int mIndex = 0;
    
    
    private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {

		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context arg0, Intent intent) {
			final String action = intent.getAction();
			Log.d("hh","################"+action+"#####################");
			 if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				 displayGattServices(getSupportedGattServices());
	               if(mNotifyCharacteristic == null ||
	            		   mWriteCharacteristic == null) {
	            	   
	               } else {

	                   final int charaProp = mNotifyCharacteristic.getProperties();
	                   if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
	                       Log.d("BluetoothLeService"," listen  notify!!! ");
	                       setCharacteristicNotification(
	                    		   mNotifyCharacteristic, true);
	                       
	                   }
	                   
	               }
	               
			 } else if(BluetoothLeService.INDI_ACTION_DATA_AVAILABLE.equals(action)){
				//if(BluetoothLeService.INDI_ACTION_DATA_AVAILABLE.equals(action)){
	            	byte[] array= intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
	            	if(array != null && array.length>0){
	            		String res = Utils.bytesToHexString(array);
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
	                			//int cmd = Device.EmCmdId.ECI_resp_init_VALUE;
	                			int cmd = Device.EmCmdId.ECI_resp_auth_VALUE;
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
	                			/*if(mWriteCharacteristic != null){
	                				Log.d("BluetoothLeService",
	                						"write auth response to watch ");
	                				writeRXCharacteristic(mWriteCharacteristic, merged);
	                			}*/
	                			readsteprequest(Contant.EXCHEER_PROTOCOL_VERSION,seq);
	                		} else if(cmdId == Device.EmCmdId.ECI_req_init.getNumber()){
	                			Log.d("BluetoothLeService","meet ECI_req_init request");
	                			
	                			Device.InitResponse.Builder  builder = Device.InitResponse.newBuilder();
	                			
	                			Device.BaseResponse.Builder baseresbuilder = Device.BaseResponse.newBuilder();
	                			baseresbuilder.setErrCode(0);
	                			baseresbuilder.setErrMsg("success");
	                			builder.setBaseResponse(baseresbuilder.build());
	                			builder.setUserIdHigh(1);
	                			builder.setUserIdLow(1);
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
	                						"write init response to watch ");
	                				writeRXCharacteristic(mWriteCharacteristic, merged);
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
		                    			case Contant.EXCHEER_CmdId__READSTEP:
		                    			{
	
		                    			        short steps = (short)(((facData[2]&0xff)<<8) | (facData[3]&0xff));
		                    			        Log.d("BluetoothLeService","GET STEPS is "+steps);
		                    			        if(steps >=5){
		                    			        	Intent testIntent = new Intent(Contant.TEST_SUCCEEDED);
		                    			        	testIntent.putExtra("teststeps", (int)steps);
			                    					sendBroadcast(testIntent);
		                    			        }else{
		                    			        	Intent testIntent = new Intent(Contant.TEST_FAILED);
		                    			        	testIntent.putExtra("teststeps", (int)steps);
			                    					sendBroadcast(testIntent);
		                    			        }
		                    			        Toast.makeText(
		                    			        BluetoothLeService.this, "STEP "+steps,
		                    			        Toast.LENGTH_SHORT)
		                    			        .show();
		                    			        controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,seq,
		                    			                        (byte)1,(byte)0,(byte)1);
	
		                    			        handler.sendEmptyMessageDelayed(1000, 2000);
		                    			        //disconnect();
		                    			        break;
		                    			} 
	                    				case  Contant.EXCHEER_CmdId__VERSION:
	                    				{
	                    					short version = (short)(((facData[2]&0xff)<<8) | (facData[3]&0xff));
	                    					char model[] = new char[3];
	                    					model[0] = (char)facData[4];
	                    					model[1] = (char)facData[5];
	                    					model[2] = (char)facData[6];
	                    					Log.d("BluetoothLeService","version "+version);
	                    					User.setDeviceVersion(BluetoothLeService.this, (int)version);
	                    					sendCMDResponse( Contant.EXCHEER_PROTOCOL_VERSION, facData[1], Contant.CMD_RES_OK,seq);	
	                    				}
	                    					break;
	                    				case Contant.EXCHEER_CmdId__BATTERY:
	                    				{
	                    					byte bl = facData[2];
	                    					Log.d("BluetoothLeService","battery "+bl);
	                    					sendCMDResponse( Contant.EXCHEER_PROTOCOL_VERSION, facData[1], Contant.CMD_RES_OK,seq);	
	                    					
	                    					// 
	                    					User.setPowerPercent(BluetoothLeService.this,(int)bl);
	                    					
	                    					User.setSyncTime(BluetoothLeService.this, new Date().getTime());
	                    					
	                    					
	                    					
	                    					/*String storeDeviceAddress = 
	                    							BLUtils.getStringValue(BluetoothLeService.this, "bindmacaddr", "");
	                    					if(storeDeviceAddress == null || storeDeviceAddress.isEmpty()) {
	                    						BLUtils.setStringValue(BluetoothLeService.this,
	                    									"bindmacaddr", mBluetoothDeviceAddress);
//	                    						BLUtils.setStringValue(BluetoothLeService.this,
//	                									"bindname", mDeviceName);
	                    					}*/ 
	                    					
	                    					Intent syncokIntent = new Intent(Contant.SYNC_OK_INTENT);
	                    					sendBroadcast(syncokIntent);
	                    					
	                    					/*handler.sendEmptyMessage(SYNC_OK);
	                    					Intent syncokIntent = new Intent(Contant.SYNC_OK_INTENT);
	                    					BluetoothLeService.this.sendBroadcast(syncokIntent);
	                    					mWaitingSync = false;
	                    					handler.removeMessages(BEGIN_SYNC_TIMER);
	                    					handler.removeMessages(BEGIN_RESYNC_TIMER);*/
	                    					Log.d("debug24","smscomingFlag:"+smscomingFlag);
	                    					if(incomingFlag || smscomingFlag){
	                    						if(incomingFlag){
	                    							incomingFlag = false;
	                    							cycle_max = 3;
		                    						ledcorlor = 1;
		                    						handler.sendEmptyMessageDelayed(0, 1000);
	                    						}
	                    						if(smscomingFlag){
	                    							smscomingFlag = false;
	                    							cycle_max = 5;
		                    						ledcorlor = 0;
		                    						handler.sendEmptyMessageDelayed(0, 1000);
	                    						}
	                    					}else{
	                    						disconnect();
	                    					}
	                    					
	                    				}
	                    					break;
	                    				case Contant.EXCHEER_CmdId__TIME:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__FIRMWARELEN:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__STEP2:
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
	                    						int pos = 8 + 8*i;
	                    						byte[] endandsteptime = new byte[6];
	                    						endandsteptime[0] = facData[i*6+8+0];
	                    						endandsteptime[1] = facData[i*6+8+1];
	                    						endandsteptime[2] = facData[i*6+8+2];
	                    						endandsteptime[3] = facData[i*6+8+3];
	                    						endandsteptime[4] = facData[i*6+8+4];
	                    						endandsteptime[5] = facData[i*6+8+5];
	                    						
	                    						String endandsteptimestr = Utils.bytesToHexString(endandsteptime);
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
	                    						short stepType;
	                    						/*if (((facData[pos + 4]&0xff)>>7) == 1) {
	                    							sleep = true;
	                    							steps = (short)(((facData[pos + 4] & 0x7F) << 8) | (facData[pos + 5]&0xff));
	                    						}
	                    						else {
	                    							sleep = false;
	                    							steps = (short)(((facData[pos + 4]&0x7f) << 8) | (facData[pos + 5]&0xff));
	                    						}*/
	                    						steps = (short)(((facData[pos + 4]&0xff) << 8) | (facData[pos + 5]&0xff));
	                    						stepType = (short)(((facData[pos + 6]&0xff) << 8) | (facData[pos + 7]&0xff));
	                    						
	                    						///short step = 
	                    						//		(short) ((short)facData[i*6+8+4]<<8| facData[i*6+8+5]);
	                    						Log.d("BluetoothLeService"," end "+ts+" step "+steps+" sleep "+sleep);
	                    						
	                    						if (i == 0) {
	                    							StepFacade.addStep(
	                    									BluetoothLeService.this, (startTime)*1000, (ts)*1000, 
	                    									/*sleep ? Steps.STEP_TYPE_SLEEPING : Steps.STEP_TYPE_WALKING*/
	                    									(int)stepType, 
	                    											steps);
	                    						} else {
	                    							StepFacade.addStep(
	                    									BluetoothLeService.this, (lastEndTime)*1000, (ts)*1000, 
	                    									(int)stepType /*sleep ? Steps.STEP_TYPE_SLEEPING : Steps.STEP_TYPE_WALKING*/,
	                    											steps);
	                    						}
	                    						
	                    						
	                    						lastEndTime = ts;
	                    					}
	                    					
	                    					///////// send step response
	                    					{

	                        					byte[] data = new byte[4];
	                        					data[0] = Contant.EXCHEER_PROTOCOL_VERSION;
	                        					data[1] = Contant.EXCHEER_CmdId__STEP2;
	                        					data[2] = 0;
	                        					data[3] = 0;
	                        					
	                                			
	                        					Device.SendDataToManufacturerSvrResponse  respose =  
	                        							Utils.buildForFacBuffer(data);
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
	                                				writeRXCharacteristic(mWriteCharacteristic,
	                                						merged);
	                                			}
	                    					}
	                    				
	                    				}
	                    					break;
	                    				case Contant.EXCHEER_CmdId__STEP:
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
	                    						
	                    						String endandsteptimestr = Utils.bytesToHexString(endandsteptime);
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
	                    									BluetoothLeService.this, (startTime)*1000, (ts)*1000, 
	                    									sleep ? Steps.STEP_TYPE_SLEEPING : Steps.STEP_TYPE_WALKING, 
	                    											steps);
	                    						} else {
	                    							StepFacade.addStep(
	                    									BluetoothLeService.this, (lastEndTime)*1000, (ts)*1000, 
	                    									sleep ? Steps.STEP_TYPE_SLEEPING : Steps.STEP_TYPE_WALKING,
	                    											steps);
	                    						}
	                    						
	                    						
	                    						lastEndTime = ts;
	                    					}
	                    					
	                    					///////// send step response
	                    					{

	                        					byte[] data = new byte[4];
	                        					data[0] = Contant.EXCHEER_PROTOCOL_VERSION;
	                        					data[1] = Contant.EXCHEER_CmdId__STEP;
	                        					data[2] = 0;
	                        					data[3] = 0;
	                        					
	                                			
	                        					Device.SendDataToManufacturerSvrResponse  respose =  
	                        							Utils.buildForFacBuffer(data);
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
	                                				writeRXCharacteristic(mWriteCharacteristic,
	                                						merged);
	                                			}
	                    					}
	                    				}
	                    					break;
	                    				case Contant.EXCHEER_CmdId__FIRMWAREREQUEST:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__FIRMWARE:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__REQUESTTIME:
	                    				{
	                    					byte[] data = new byte[7];
	                    					data[0] = Contant.EXCHEER_PROTOCOL_VERSION;
	                    					data[1] = Contant.EXCHEER_CmdId__TIME;
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
	                    							Utils.buildForFacBuffer(data);
	                    					
	                    					//respose.
	                    					byte[] result = respose.toByteArray();
	                    					
	                    					
	                    					
	                    					byte[] merged = Utils.mergeFromFac(result,
	                    							Device.EmCmdId.ECI_resp_sendDataToManufacturerSvr_VALUE,
	                    							seq);
	                    					String resultHex = Utils.bytesToHexString(merged);
	                    					
	                    					Log.d("BluetoothLeService",
	                        						"resultHex " +resultHex);
	                    					
	                    					if(mWriteCharacteristic != null){
	                            				Log.d("BluetoothLeService",
	                            						"write fac response to watch ");
	                            				writeRXCharacteristic(mWriteCharacteristic,
	                            						merged);
	                            			}
	                    				}
	                    					break;
	                    				case Contant.EXCHEER_CmdId__LOG:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__SETTING:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__DEBUG:
	                    					break;
	                    				case Contant.EXCHEER_CmdId__DEBUGTOSERVER:
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
	            	
	            
			 }else //如果是拨打电话
		            /*if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){                        
	                    incomingFlag = false;
	                    String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	                    Toast.makeText(BluetoothLeService.this, "call OUT:"+phoneNumber, Toast.LENGTH_SHORT).show();
	                    Log.i(TAG, "call OUT:"+phoneNumber);                        
	            }else */if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){                  
	                    //如果是来电
	            	Boolean phone_flag = BLUtils.getBooleanValue(BluetoothLeService.this, "phone_flag", true);
	            	Log.d(TAG,"phone_flag:"+phone_flag);
	            	if(phone_flag){//是否开启来电提醒
	            		
		         	            
		         	            	TelephonyManager tm = 
		         	                        (TelephonyManager)BluetoothLeService.this.getSystemService(Service.TELEPHONY_SERVICE);                        
		         	                    
		         	                    switch (tm.getCallState()) {
		         	                    case TelephonyManager.CALL_STATE_RINGING://来电
		         	                    	if(connect(mBluetoothDeviceAddress)){
		         	                            incomingFlag = true;//标识当前是来电
		         	                            incoming_number = intent.getStringExtra("incoming_number");
		         	                            Toast.makeText(BluetoothLeService.this, "sendCallTip\n"+"RINGING :"+ incoming_number, Toast.LENGTH_SHORT).show();
		         	                            Log.i(TAG, "RINGING :"+ incoming_number);
		         	                    	}
		         	                            break;
		         	                    case TelephonyManager.CALL_STATE_OFFHOOK://摘机（正在通话中）                                
		         	                            if(incomingFlag){
		         	                            		Toast.makeText(BluetoothLeService.this, "incoming ACCEPT :"+ incoming_number, Toast.LENGTH_SHORT).show();
		         	                                    Log.i(TAG, "incoming ACCEPT :"+ incoming_number);
		         	                            }
		         	                            break;
		         	                    
		         	                    case TelephonyManager.CALL_STATE_IDLE://空闲                                
		         	                            if(incomingFlag){
		         	                            		Toast.makeText(BluetoothLeService.this, "incoming IDLE", Toast.LENGTH_SHORT).show();
		         	                                    Log.i(TAG, "incoming IDLE");                                
		         	                            }
		         	                            break;
		         	                    }
		         	            
		         	        }else {
			            		Toast.makeText(BluetoothLeService.this, BluetoothLeService.this.getResources().getString(R.string.not_find_fastfox_service), Toast.LENGTH_SHORT).show();
			            	}
		            	
	            	
		            	
	                    
	            }else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){//收到短信
	            	Boolean phone_flag = BLUtils.getBooleanValue(BluetoothLeService.this, "phone_flag", true);
	            	Log.d("debug23","phone_flag:"+phone_flag);
	            	if(phone_flag){//是否开启来电提醒
	            		if(connect(mBluetoothDeviceAddress)){
	            				smscomingFlag = true;
	    	     	            Toast.makeText(BluetoothLeService.this, "SMS_RECEIVED", Toast.LENGTH_SHORT).show();
	    	     	            Log.i(TAG, "---SMS_RECEIVED---");
	            		}
	    	        }else{
	    	            		Toast.makeText(BluetoothLeService.this, BluetoothLeService.this.getResources().getString(R.string.not_find_fastfox_service), Toast.LENGTH_SHORT).show();
	    	            }
	            }
	            	
	            
		}
    	
    };
    @SuppressLint("NewApi") private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
       // String unknownServiceString = getResources().getString(R.string.unknown_service);
       // String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
       // mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

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
           // mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
    
    private static IntentFilter makeLocalIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.INDI_ACTION_DATA_AVAILABLE);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);//来电
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);//去电
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");//短信
       
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
		writeRXCharacteristic(mWriteCharacteristic,
				merged);
	}
		return true;
    }
    private boolean controlWatchLight( byte cmd_version, int seq,byte onoff, byte index, byte color) {
    	Log.d("BluetoothLeService",
				"---controlWatchLight---");
		byte cmd[] = new byte[5];
		cmd[0] = cmd_version;
		cmd[1] = Contant.EXCHEER_CmdId__LIGHT;
		
		// result
		cmd[2] = onoff;
		cmd[3] = index;
		cmd[4] = color;
		
		Device.ManufacturerSvrSendDataPush  respose =  
				Utils.buildForPushBuffer(cmd);
		byte[] result = respose.toByteArray();
		
		
		
		byte[] merged = Utils.mergeFromFac(result,
				Device.EmCmdId.ECI_push_manufacturerSvrSendData_VALUE,
				seq);
		String resultHex = Utils.bytesToHexString(merged);
		
		Log.d("BluetoothLeService",
				"resultHex " +resultHex);
		
		if(mWriteCharacteristic != null){
		Log.d("BluetoothLeService",
				"write push  to watch ");
		writeRXCharacteristic(mWriteCharacteristic,
				merged);
	}
		return true;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        
        
       
        Log.d("BluetoothLeService",
				"---onCreate---  ");
        initialize();
        
        registerReceiver(mLocalReceiver, makeLocalIntentFilter());
        
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent,startId);
        Log.d("BluetoothLeService","---onStart---");
        if(intent!= null ){
        	//mBluetoothDeviceAddress = 
        	mBluetoothDeviceAddress = intent.getStringExtra("bindmacaddr");
        	mBluetoothDeviceName = intent.getStringExtra("bindname");
        }
        if(mBluetoothDeviceAddress  ==null) {
        	mBluetoothDeviceAddress = BLUtils.getStringValue(BluetoothLeService.this, "bindmacaddr", "");
		}
        Log.d("debug27","mBluetoothDeviceAddress:"+mBluetoothDeviceAddress);
        connect(mBluetoothDeviceAddress);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BluetoothLeService",
				"---onDestroy---  ");
        unregisterReceiver(mLocalReceiver);
    }
    public Handler handler = new Handler(){
    	int count = 0;
		public void handleMessage(Message msg){
			Bundle bundle = msg.getData();
			switch (msg.what){
			case BLUETOOTHLESEVICE_DISCONNECT:
				disconnect();
				break;
			case 0:
				Log.d("debug24","---handler---"+0);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)1,(byte)0,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(1111,300);
				break;
			case 3:
				Log.d("debug24","---handler---"+3);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)1,(byte)3,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(3333,300);
				break;
			case 6:
				Log.d("debug24","---handler---"+6);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)1,(byte)6,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(6666,300);
				break;
			case 9:
				Log.d("debug24","---handler---"+9);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)1,(byte)9,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(9999,300);
				break;
			case 1111:
				Log.d("debug24","---handler---"+1111);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)0,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(3,300);
				break;
			case 3333:
				Log.d("debug24","---handler---"+3333);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)3,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(6,300);
				break;
			case 6666:
				Log.d("debug24","---handler---"+6666);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)6,(byte)ledcorlor);
				handler.sendEmptyMessageDelayed(9,300);
				break;
			case 9999:
				Log.d("debug24","---handler---"+9999);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)9,(byte)ledcorlor);
				count ++;
				Log.d("debug24","count:"+count);
				if(count >= cycle_max){
					handler.sendEmptyMessageDelayed(10000,300);
				}else {
					handler.sendEmptyMessageDelayed(0,300);
				}				
				break;
			case 10000:
				Log.d("debug24","---handler---"+10000);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)0,(byte)ledcorlor);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)3,(byte)ledcorlor);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)6,(byte)ledcorlor);
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,(byte)0,(byte)9,(byte)ledcorlor);
				count = 0;
				disconnect();
				break;
			case 1000:
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,
						(byte)0,(byte)0,(byte)1);
				
				handler.sendEmptyMessageDelayed(2000, 2000);
				break;
			case 2000:
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,
						(byte)1,(byte)0,(byte)0);
				
				handler.sendEmptyMessageDelayed(3000, 2000);
				break;
			case 3000:
				controlWatchLight(Contant.EXCHEER_PROTOCOL_VERSION,5000,
						(byte)0,(byte)0,(byte)0);
				handler.sendEmptyMessageDelayed(4000, 2000);
				break;
			case 4000:
				disconnect();
				break;
			default:
			      break;
			}
		}
    };
    private boolean readsteprequest( byte cmd_version, int seq) {
    	Log.d("BluetoothLeService",
				"---controlWatchLight---");
		byte cmd[] = new byte[2];
		cmd[0] = cmd_version;
		cmd[1] = Contant.EXCHEER_CmdId__READSTEP;
		
		
		Device.ManufacturerSvrSendDataPush  respose =  
				Utils.buildForPushBuffer(cmd);
		byte[] result = respose.toByteArray();
		
		
		
		byte[] merged = Utils.mergeFromFac(result,
				Device.EmCmdId.ECI_push_manufacturerSvrSendData_VALUE,
				seq);
		String resultHex = Utils.bytesToHexString(merged);
		
		Log.d("BluetoothLeService",
				"resultHex " +resultHex);
		
		if(mWriteCharacteristic != null){
		Log.d("BluetoothLeService",
				"write push  to watch ");
		writeRXCharacteristic(mWriteCharacteristic,
				merged);
	}
		return true;
    }
}
