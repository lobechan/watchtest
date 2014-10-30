package com.fastfox.watchassistant;

public class SleepDetailData {
	public static final String SLEEP_WALK = "walk";
	public static final String SLEEP_DEEP = "deep";
	
	private long startTime;
	private long endTime;
	private String sleepType;
	
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public String getSleepType() {
		return sleepType;
	}
	public void setSleepType(String sleepType) {
		this.sleepType = sleepType;
	}
}
