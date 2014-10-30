package com.fastfox.watchassistant;

import java.io.Serializable;

public class Steps implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int STEP_TYPE_WALKING = 1;
	public static final int STEP_TYPE_SLEEPING = 2;
	
	public static final int STEP_NEW_TYPE_SLEEP = 9;
	public static final int STEP_NEW_TYPE_WALK = 10;
	public static final int STEP_NEW_TYPE_RUN = 11;
	public static final int STEP_NEW_TYPE_WHEEL = 12;
	
	private long id;
	private long startTime;
	private long endTime;
	private long createTime;
	private long deviceId = 0;
	private long ffUserId = 0;
	private int stepType;
	private int steps;
	
	public String getMapKey() {
		return startTime + "_" + endTime + "_" + deviceId + "_" + stepType + "_" + steps;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}
	public int getStepType() {
		return stepType;
	}
	public void setStepType(int stepType) {
		this.stepType = stepType;
	}
	public int getSteps() {
		return steps;
	}
	public void setSteps(int steps) {
		this.steps = steps;
	}

	public long getFfUserId() {
		return ffUserId;
	}

	public void setFfUserId(long ffUserId) {
		this.ffUserId = ffUserId;
	}

}
