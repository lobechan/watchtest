package com.bluefay.core;

public class BLMeasure {
	private long mStartTime;
	private long mEndTime;
	private String mAction;

	public BLMeasure() {
		mAction = "";
	}

	public BLMeasure(String action) {
		mAction = action;
	}

	public void start() {
		mStartTime = System.currentTimeMillis();
	}

	public void end() {
		mEndTime = System.currentTimeMillis();
		BLLog.i("%s total %d ms", mAction, mEndTime - mStartTime);
	}
}
