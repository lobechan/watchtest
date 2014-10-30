package com.fastfox.watchassistant;

import java.util.ArrayList;
import java.util.List;


public class SleepData {
	//private static Log log = LogFactory.getLog(SleepData.class);
	
	public static final String DATA_LIVE = "live";
	public static final String DATA_SLEEP = "sleep";
	public static final String TIME_SPAN_DAY = "day";
	public static final String TIME_SPAN_WEEK = "week";
	public static final String TIME_SPAN_MONTH = "month";
	
	private String dataType = "";
	private long startTime = 0;
	private long endTime = 0;
	private boolean isNow = false;
	private double sleepMinutes = 0;
	private double deepMinutes = 0;
	private double walkMinutes = 0;
	private double targetMinutes = 0;
	private String timeSpan = "";
	private List<SleepDetailData> detailData = new ArrayList<SleepDetailData>();
	private List<SleepTimeData> timeData = new ArrayList<SleepTimeData>();
	
	
	private void printTimeData() {
		for (SleepTimeData ltd : timeData) {
			//log.info("index: " + ltd.getTimeIndex() + ", steps: " + ltd.getSleepMinutes());
		}
	}
	
	public void fixTimeData(double maxTimeIndex) {
		List<SleepTimeData> tmp = timeData;
		timeData = new ArrayList<SleepTimeData>();
		double cur = -1;
		for (SleepTimeData ltd : tmp) {
			if (ltd.getTimeIndex() - cur > 1) {
				for (double i = cur + 1; i < ltd.getTimeIndex(); ++i) {
					SleepTimeData l = new SleepTimeData();
					l.setTimeIndex(i);
					l.setSleepMinutes(0);
					l.setDeepSleepMinutes(0);
					timeData.add(l);
				}
			}
			timeData.add(ltd);
			cur = ltd.getTimeIndex();
		}
		for (double i = cur + 1; i <= maxTimeIndex; ++i) {
			SleepTimeData l = new SleepTimeData();
			l.setTimeIndex(i);
			l.setSleepMinutes(0);
			l.setDeepSleepMinutes(0);
			timeData.add(l);
		}
	}
	
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
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
	public boolean isNow() {
		return isNow;
	}
	public void setNow(boolean isNow) {
		this.isNow = isNow;
	}
	public String getTimeSpan() {
		return timeSpan;
	}
	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}
	public List<SleepDetailData> getDetailData() {
		return detailData;
	}
	public void setDetailData(List<SleepDetailData> detailData) {
		this.detailData = detailData;
	}
	public List<SleepTimeData> getTimeData() {
		return timeData;
	}
	public void setTimeData(List<SleepTimeData> timeData) {
		this.timeData = timeData;
	}

	public double getSleepMinutes() {
		return sleepMinutes;
	}

	public void setSleepMinutes(double sleepMinutes) {
		this.sleepMinutes = sleepMinutes;
	}

	public double getDeepMinutes() {
		return deepMinutes;
	}

	public void setDeepMinutes(double deepMinutes) {
		this.deepMinutes = deepMinutes;
	}

	public double getTargetMinutes() {
		return targetMinutes;
	}

	public void setTargetMinutes(double targetMinutes) {
		this.targetMinutes = targetMinutes;
	}

	public double getWalkMinutes() {
		return walkMinutes;
	}

	public void setWalkMinutes(double walkMinutes) {
		this.walkMinutes = walkMinutes;
	}
}
