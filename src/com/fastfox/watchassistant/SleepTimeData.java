package com.fastfox.watchassistant;

public class SleepTimeData {
	private double timeIndex = 0;
	private double sleepMinutes = 0;
	private double deepSleepMinutes = 0;
	public double getTimeIndex() {
		return timeIndex;
	}
	public void setTimeIndex(double timeIndex) {
		this.timeIndex = timeIndex;
	}
	public double getSleepMinutes() {
		return sleepMinutes;
	}
	public void setSleepMinutes(double sleepMinutes) {
		this.sleepMinutes = sleepMinutes;
	}
	public double getDeepSleepMinutes() {
		return deepSleepMinutes;
	}
	public void setDeepSleepMinutes(double deepSleepMinutes) {
		this.deepSleepMinutes = deepSleepMinutes;
	}
	
}

