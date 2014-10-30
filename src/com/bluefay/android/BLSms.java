package com.bluefay.android;

import java.util.ArrayList;

import com.bluefay.core.BLLog;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.telephony.SmsManager;

public class BLSms {

	public static final String MESSAGE_DELIVERY_ACTION = "com.bluefay.freemessage.MESSAGE_DELIVERY";
	public static final String MESSAGE_SENT_ACTION = "com.bluefay.freemessage.MESSAGE_SENT";

	private Context mContext;
	private Listener mListener;
	private int mMessageId = 0;

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BLLog.d(action);
			if (action.equals(MESSAGE_SENT_ACTION)) {
				Uri uri = intent.getData();
				int resultCode = getResultCode();
				int id = Integer.parseInt(uri.getLastPathSegment());
				BLLog.d("id:%d,res:%d", id, resultCode);
				if (mListener != null) {
					if (resultCode == Activity.RESULT_OK) {
						mListener.onMessageSent(id, 55);
					} else {
						mListener.onMessageSent(id, 0);
					}
				}
			}
		}
	};

	public BLSms(Context context) {
		mContext = context;
	}

	public void startListener(Listener listener) {
		mListener = listener;
		IntentFilter filter = new IntentFilter();
		filter.addAction(MESSAGE_SENT_ACTION);
		mContext.registerReceiver(mIntentReceiver, filter);
	}

	public void stopListener() {
		mListener = null;
		mContext.unregisterReceiver(mIntentReceiver);
	}

	public void send(String number, String text) {
		ArrayList<String> bodys = null;
		bodys = SmsManager.getDefault().divideMessage(text);
		if (bodys == null || bodys.size() == 0) {
			return;
		}
		mMessageId++;

		// int count = bodys.size();
		// ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(
		// count);
		// Uri uri = Uri.parse("content://sms/" + String.valueOf(mMessageId));
		// for (int i = 0; i < count; i++) {
		// PendingIntent sentIntent = PendingIntent.getBroadcast(mContext, 0,
		// new Intent(MESSAGE_SENT_ACTION, uri, mContext, null), 0);
		// sentIntents.add(sentIntent);
		// }
		SmsManager.getDefault().sendMultipartTextMessage(number, null, bodys,
				null, null);
	}

	public interface Listener {

		public void onMessageSent(int id, int reference_number);

	}
}
