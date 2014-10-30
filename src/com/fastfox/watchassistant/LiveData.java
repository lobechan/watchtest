package com.fastfox.watchassistant;

import java.util.ArrayList;
import java.util.List;


public class LiveData {
	
	public static final String DATA_LIVE = "live";
	public static final String DATA_SLEEP = "sleep";
	public static final String TIME_SPAN_DAY = "day";
	public static final String TIME_SPAN_WEEK = "week";
	public static final String TIME_SPAN_MONTH = "month";
	
	private String dataType = "";
	private long startTime = 0;
	private long endTime = 0;
	private boolean isNow = false;
	private int walkSteps = 0;
	private int walkDistance = 0;
	private int walkKiloCalorie = 0;
	private int walkSeconds = 0;
	private int runSteps = 0;
	private int runDistance = 0;
	private int runKiloCalorie = 0;
	private int runSeconds = 0;
	private int totalTarget = 0;
	private String timeSpan = "";
	private List<LiveDetailData> detailData = new ArrayList<LiveDetailData>();
	private List<LiveTimeData> timeData = new ArrayList<LiveTimeData>();
	private int wheelSteps = 0;
	private int wheelSeconds = 0;
	
	private void printTimeData() {
		for (LiveTimeData ltd : timeData) {
			//log.info("index: " + ltd.getTimeIndex() + ", steps: " + ltd.getSteps());
		}
	}
	
	public void fixTimeData(int maxTimeIndex) {
		List<LiveTimeData> tmp = timeData;
		timeData = new ArrayList<LiveTimeData>();
		int cur = -1;
		for (LiveTimeData ltd : tmp) {
			if (ltd.getTimeIndex() - cur > 1) {
				for (int i = cur + 1; i < ltd.getTimeIndex(); ++i) {
					LiveTimeData l = new LiveTimeData();
					l.setSteps(0);
					l.setTimeIndex(i);
					l.setTimeUint(ltd.getTimeUint());
					timeData.add(l);
				}
			}
			timeData.add(ltd);
			cur = ltd.getTimeIndex();
		}
		for (int i = cur + 1; i <= maxTimeIndex; ++i) {
			LiveTimeData l = new LiveTimeData();
			l.setSteps(0);
			l.setTimeIndex(i);
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
	public int getWalkSteps() {
		return walkSteps;
	}
	public void setWalkSteps(int walkSteps) {
		this.walkSteps = walkSteps;
	}
	public int getWalkDistance() {
		return walkDistance;
	}
	public void setWalkDistance(int walkDistance) {
		this.walkDistance = walkDistance;
	}
	public int getRunSteps() {
		return runSteps;
	}
	public void setRunSteps(int runSteps) {
		this.runSteps = runSteps;
	}
	public int getRunDistance() {
		return runDistance;
	}
	public void setRunDistance(int runDistance) {
		this.runDistance = runDistance;
	}
	public String getTimeSpan() {
		return timeSpan;
	}
	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}
	public List<LiveDetailData> getDetailData() {
		return detailData;
	}
	public void setDetailData(List<LiveDetailData> detailData) {
		this.detailData = detailData;
	}
	public List<LiveTimeData> getTimeData() {
		return timeData;
	}
	public void setTimeData(List<LiveTimeData> timeData) {
		this.timeData = timeData;
	}
	public int getTotalTarget() {
		return totalTarget;
	}
	public void setTotalTarget(int totalTarget) {
		this.totalTarget = totalTarget;
	}
	public int getWalkKiloCalorie() {
		return walkKiloCalorie;
	}
	public void setWalkKiloCalorie(int walkKiloCalorie) {
		this.walkKiloCalorie = walkKiloCalorie;
	}
	public int getRunKiloCalorie() {
		return runKiloCalorie;
	}
	public void setRunKiloCalorie(int runKiloCalorie) {
		this.runKiloCalorie = runKiloCalorie;
	}
	public int getWalkSeconds() {
		return walkSeconds;
	}
	public void setWalkSeconds(int walkSeconds) {
		this.walkSeconds = walkSeconds;
	}
	public int getRunSeconds() {
		return runSeconds;
	}
	public void setRunSeconds(int runSeconds) {
		this.runSeconds = runSeconds;
	}
	public int getWheelSteps() {
		return wheelSteps;
	}

	public void setWheelSteps(int wheelSteps) {
		this.wheelSteps = wheelSteps;
	}

	public int getWheelSeconds() {
		return wheelSeconds;
	}

	public void setWheelSeconds(int wheelSeconds) {
		this.wheelSeconds = wheelSeconds;
	}
}
