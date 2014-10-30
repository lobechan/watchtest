package com.excheer.watchassistant.version;

import java.io.Serializable;

public class VersionModel implements Serializable {

	private static final long serialVersionUID = 2880513242326924897L;
	private int status;
	private int versionCode;
	private String versionName;
	private String url;
	private String description;
	private String tag;
	private boolean forceUpdate;
	private int retCode;

	public VersionModel() {
	}

	public VersionModel(int status, int versionCode, String versionName,
			String url, String description, String tag, boolean forceUpdate,
			int retCode) {
		super();
		this.status = status;
		this.versionCode = versionCode;
		this.versionName = versionName;
		this.url = url;
		this.description = description;
		this.tag = tag;
		this.forceUpdate = forceUpdate;
		this.retCode = retCode;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	@Override
	public String toString() {
		return "status:" + status + ", versionCode:" + versionCode
				+ ", versionName:" + versionName + ", url:" + url
				+ ", description:" + description + ", tag:" + tag
				+ ", forceUpdate" + forceUpdate + ", retCode:" + retCode;
	}

}
