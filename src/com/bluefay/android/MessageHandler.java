package com.bluefay.android;

import android.os.Handler;

public class MessageHandler extends Handler {

	private int[] mMsgIDs;

	public MessageHandler(int[] ids) {
		super();
		mMsgIDs = ids;
	}

	public boolean support(int id) {
		if (mMsgIDs == null) {
			return false;
		}
		for (int oneid : mMsgIDs) {
			if (oneid == id) {
				return true;
			}
		}
		return false;
	}
}
