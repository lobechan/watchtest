package com.bluefay.android;

public interface Callback {

	public final static int ERROR = 0;
	public final static int SUCCESS = 1;
	public final static int CANCEL = 2;

	public final static int ERROR_NETWORK = 100;
	public final static int ERROR_FILESYSTEM = 200;
	public final static int ERROR_JSONDATA = 300;
	public final static int ERROR_TOKEN = 400;

	public void run(int retcode, Object data);
}
