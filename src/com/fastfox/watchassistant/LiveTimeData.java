package com.fastfox.watchassistant;

public class LiveTimeData {
	public static final String TIME_UNIT_HOUR = "hour";
	public static final String TIME_UNIT_DAY = "day";
	
	private String timeUint = "";
	private int timeIndex = 0;
	private int steps = 0;
	
	public void init(String timeUnit) {
		this.timeUint = timeUnit;
		this.timeIndex = 0;
		this.steps = 0;
	}
	
	public String getTimeUint() {
		return timeUint;
	}
	public void setTimeUint(String timeUint) {
		this.timeUint = timeUint;
	}
	public int getTimeIndex() {
		return timeIndex;
	}
	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}
	public int getSteps() {
		return steps;
	}
	public void setSteps(int steps) {
		this.steps = steps;
	}
}

