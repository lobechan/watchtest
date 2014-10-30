package com.bluefay.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.util.Log;

public class BLHttp {

	public static final int OK = 0;
	public static final int NETWORK_ERROR = 1;
	public static final int SERVER_ERROR = 2;
	public static final int OTHER_ERROR = 3;

	public static final int SOCKET_CONNECT_TIMEOUT = 10000;
	public static final int SOCKET_READ_TIMEOUT = 30000;

	public interface BLHttpListener {

		public void downloadFinished(int result);

		public void downloadProgress(int receiveSize, int total);

		public void uploadProgress(int postSize, int total);

		public void uploadFinished(int result);
	}

	private String mUrl;
	private String mProxyAddress;
	private int mProxyPort;
	private ArrayList<BLHttpListener> mListenerList = new ArrayList<BLHttpListener>();
	private HttpURLConnection mConnection;

	public BLHttp(String url) {
		mUrl = url;
	}

	public void addListener(BLHttpListener listener) {
		mListenerList.add(listener);
	}

	public void setProxy(String address, int port) {
		mProxyAddress = address;
		mProxyPort = port;
	}

	public String getProxyAddress() {
		return mProxyAddress;
	}

	public int getProxyPort() {
		return mProxyPort;
	}

	private void post(URLConnection conn, byte[] data) throws IOException {
		Log.d("post","---post3---");
		int write = 0;
		int maxlen = data.length;
		OutputStream os = conn.getOutputStream();

		for (BLHttpListener listener : mListenerList) {
			listener.uploadProgress(0, maxlen);
		}

		while (write < maxlen) {
			if (maxlen - write > 4096) {
				os.write(data, write, 4096);
				write += 4096;
			} else {
				os.write(data, write, maxlen - write);
				write = maxlen;
			}
			for (BLHttpListener listener : mListenerList) {
				listener.uploadProgress(write, maxlen);
			}
		}
		os.close();

	}

	private byte[] download(URLConnection conn) throws IOException {
		InputStream in;

		in = conn.getInputStream();

		int filesize = conn.getContentLength();
		if (filesize <= 0) {
			filesize = -1;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len = 0;
		int download = 0;

		for (BLHttpListener listener : mListenerList) {
			listener.downloadProgress(0, filesize);
		}

		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
			download += len;
			for (BLHttpListener listener : mListenerList) {
				listener.downloadProgress(download, filesize);
			}
		}
		byte[] result = out.toByteArray();
		in.close();
		out.close();
		return result;

	}

	public byte[] get() {
		URL uRL = null;
		int rescode = OK;
		byte[] res = null;
		try {
			uRL = new URL(mUrl);
			if (mProxyAddress != null && mProxyPort > 0) {
				BLLog.d("GET Use proxy");
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						mProxyAddress, mProxyPort));
				mConnection = (HttpURLConnection) uRL.openConnection(proxy);
			} else {
				BLLog.d("GET Not use proxy");
				mConnection = (HttpURLConnection) uRL.openConnection();
			}
			mConnection.setConnectTimeout(SOCKET_CONNECT_TIMEOUT);
			mConnection.setReadTimeout(SOCKET_READ_TIMEOUT);
			if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// OK
				res = download(mConnection);
			} else if (mConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				String location = mConnection.getHeaderField("Location");
				res = BLHttp.get(location);
			} else {
				// Server returned HTTP error code.
				rescode = SERVER_ERROR;
			}
		} catch (IOException e) {
			rescode = NETWORK_ERROR;
			BLLog.e(e.getMessage());
		}
		for (BLHttpListener listener : mListenerList) {
			listener.downloadFinished(rescode);
		}
		return res;
	}

	public byte[] post(byte[] data) {
		Log.d("post","---post2---");
		URL uRL = null;
		int rescode = OK;
		byte[] res = null;
		try {
			uRL = new URL(mUrl);
			if (mProxyAddress != null && mProxyPort > 0) {
				BLLog.d("POST Use proxy");
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						mProxyAddress, mProxyPort));
				mConnection = (HttpURLConnection) uRL.openConnection(proxy);
			} else {
				BLLog.d("POST Not use proxy");
				mConnection = (HttpURLConnection) uRL.openConnection();
			}
			mConnection.setDoOutput(true);
			mConnection.setRequestMethod("POST");
			post(mConnection, data);
			Log.d("post","ResCode:"+mConnection.getResponseCode());
			if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// OK
				res = download(mConnection);
				rescode = OK;
			} else {
				// Server returned HTTP error code.
				rescode = SERVER_ERROR;
			}
		} catch (IOException e) {
			rescode = NETWORK_ERROR;
			BLLog.e(e.getMessage());
		}

		for (BLHttpListener listener : mListenerList) {
			listener.uploadFinished(rescode);
		}
		return res;
	}

	public void disconnect() {
		BLLog.d("disconnect");
		if (mConnection != null) {
			BLLog.d("Close http connection");
			mConnection.disconnect();
		}
	}

	public static byte[] get(String url, BLHttpListener callback) {
		BLHttp http = new BLHttp(url);
		http.addListener(callback);
		// String proxy = AndroidNetwork.getDataConnectionProxy();
		// if (proxy != null) {
		// BLLog.d("Proxy is:" + proxy);
		// Uri uri = Uri.parse(proxy);
		// http.setProxy(uri.getHost(), uri.getPort());
		// }
		return http.get();
	}

	public static byte[] get(String url) {
		BLHttp http = new BLHttp(url);
		// String proxy = AndroidNetwork.getDataConnectionProxy();
		// if (proxy != null) {
		// BLLog.d("Proxy is:" + proxy);
		// Uri uri = Uri.parse(proxy);
		// http.setProxy(uri.getHost(), uri.getPort());
		// }
		return http.get();
	}

	public static byte[] post(String url, byte[] data) {
		Log.d("post","---post1---");
		BLHttp http = new BLHttp(url);
		return http.post(data);
	}
}
