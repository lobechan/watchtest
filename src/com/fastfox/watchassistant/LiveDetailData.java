package com.fastfox.watchassistant;

public class LiveDetailData {
	public static final String LIVE_WALK = "walk";
	public static final String LIVE_RUN = "run";
	
	private long startTime;
	private long endTime;
	private String liveType;
	private int timeIndex = 0;
	private int steps;
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
	public String getLiveType() {
		return liveType;
	}
	public void setLiveType(String liveType) {
		this.liveType = liveType;
	}
	public int getSteps() {
		return steps;
	}
	public void setSteps(int steps) {
		this.steps = steps;
	}
	public int getTimeIndex() {
		return timeIndex;
	}
	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}
}
