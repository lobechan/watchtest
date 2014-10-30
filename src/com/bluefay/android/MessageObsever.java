package com.bluefay.android;

import java.util.ArrayList;

import com.bluefay.core.BLLog;

import android.os.Message;

public class MessageObsever {

	private ArrayList<MessageHandler> mListeners;

	public MessageObsever() {
		mListeners = new ArrayList<MessageHandler>();
	}

	public void addListener(MessageHandler listener) {
		if (listener != null) {
			mListeners.add(listener);
			BLLog.d("size:%d", mListeners.size());
		}
	}

	public void removeListener(MessageHandler listener) {
		if (listener != null) {
			mListeners.remove(listener);
			BLLog.d("size:%d", mListeners.size());
		}
	}

	public void dispatch(Message msg) {
		dispatch(msg, 0);
	}

	public void dispatch(Message msg, long delay) {
		int what = msg.what;
		for (MessageHandler handler : mListeners) {
			if (handler.support(what)) {
				Message copy = new Message();
				copy.copyFrom(msg);
				handler.sendMessageDelayed(copy, delay);
			}
		}
	}
}
