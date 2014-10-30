package com.excheer.until;

import java.util.Calendar;
import java.util.Date;

import android.util.Log;

import com.excheer.protobuf.Device;
import com.google.protobuf.ByteString;

public class Utils {

public static int getMonth(long time){
	Calendar cal1 = Calendar.getInstance();
	cal1.setTime(new Date(time));
	return cal1.get(Calendar.MONTH);
}
public static int getDay(long time){
	Calendar cal1 = Calendar.getInstance();
	cal1.setTime(new Date(time));
	return cal1.get(Calendar.DAY_OF_MONTH);
}

public static boolean isSameDay(long t1, long t2) {
	Calendar cal1 = Calendar.getInstance();
	cal1.setTime(new Date(t1));
	
	Calendar cal2 = Calendar.getInstance();
	cal2.setTime(new Date(t2));
	
	
	if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
			&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
			cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
		return true;
	}
	return false;
}

public static boolean  isSameWeek(long t1, long t2) {
	long b = t1;
	long s = t2;
	long tmp =0;
	if (b <= s) {
		tmp = b;
		b = s;
		s = tmp;
	}
	
	
	
	// over 7 days
	if (((b- s)/(24*60*60*1000)) > 6)
		return false;
	
	/*var wb = b.getDay();
	var ws = s.getDay();*/
	Calendar cal1 = Calendar.getInstance();
	cal1.setTime(new Date(b));
	
	Calendar cal2 = Calendar.getInstance();
	cal2.setTime(new Date(s));
	int wb = cal1.get(Calendar.DAY_OF_WEEK);
	int ws = cal2.get(Calendar.DAY_OF_WEEK);
	if (wb == 0) wb = 7;
	if (ws == 0) ws = 7;
	
	if (Math.abs(ws-wb) < 6) return true;
	
	return false;
}
public static boolean isSameMonth(long t1, long t2) {
	Calendar cal1 = Calendar.getInstance();
	cal1.setTime(new Date(t1));
	
	Calendar cal2 = Calendar.getInstance();
	cal2.setTime(new Date(t2));
	
	
	if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) 
			&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) {
		return true;
	}
	return false;
}
public static String bytesToHexString(byte[] src){  
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
public static byte[] mergeFromFac(byte[] facdata, int cmd, int seq){
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

public static Device.SendDataToManufacturerSvrResponse buildForFacBuffer(byte[] facBuffer){
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
public static Device.ManufacturerSvrSendDataPush buildForPushBuffer(byte[] facBuffer){
	Device.ManufacturerSvrSendDataPush.Builder  builder = 
			Device.ManufacturerSvrSendDataPush.newBuilder();

	Device.BasePush.Builder baseresbuilder = Device.BasePush.newBuilder();
	//baseresbuilder.
	//builder.
	builder.setBasePush(baseresbuilder.build());
	//builder.setData(value)
	ByteString byteString = ByteString.copyFrom(facBuffer);
	builder.setData(byteString);
	
	//builder.
	Device.ManufacturerSvrSendDataPush res = builder.build();
	
	return res;
}
}
