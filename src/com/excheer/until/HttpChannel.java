/**
 * @author jinke
 * @email jinke479150@gmail.com
 * @date Feb 24, 2012 1:44:05 PM
 */
package com.excheer.until;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class HttpChannel {
	private static final int TIMEOUT = 60;
	private static String USER_AGENT = "User-Agent";
	private boolean debug = false;
	public static String TAG = "Sync";

	private DefaultHttpClient httpClient;
	private String userAgent;

	private static HttpChannel instance = new HttpChannel();

	public HttpChannel() {
		httpClient = createHttpClient();
	}

	public static HttpChannel getInstance() {
		return instance;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setDebugMode(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Create a thread-safe client. This client does not do redirecting, to
	 * allow us to capture correct "error" codes.
	 * 
	 * @return HttpClient
	 */
	public static final DefaultHttpClient createHttpClient() {
		// Sets up the http part of the service.
		final SchemeRegistry supportedSchemes = new SchemeRegistry();

		// Register the "http" protocol scheme, it is required
		// by the default operator to look up socket factories.
		// final PlainSocketFactory sf = PlainSocketFactory.getSocketFactory();

		final PlainSocketFactory sf = PlainSocketFactory.getSocketFactory();// new
																			// PlainSocketFactory(resoler);

		supportedSchemes.register(new Scheme("http", sf, 80));
		supportedSchemes.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Set some client http client parameter defaults.
		final HttpParams httpParams = createHttpParams();
		HttpClientParams.setRedirecting(httpParams, false);

		final ClientConnectionManager ccm = new ThreadSafeClientConnManager(
				httpParams, supportedSchemes);
		DefaultHttpClient httpClient = new DefaultHttpClient(ccm, httpParams);
		return httpClient;
	}

	private static final HttpParams createHttpParams() {
		final HttpParams params = new BasicHttpParams();

		// Turn off stale checking. Our connections break all the time anyway,
		// and it's not worth it to pay the penalty of checking every time.
		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT * 1000);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		return params;
	}

	public HttpGet createHttpGet(String url, JSONObject params) {
		if (params != null) {
			url = url + "?" + params.toString();
		}
		HttpGet httpGet = new HttpGet(url);
		if (userAgent != null) {
			httpGet.addHeader(USER_AGENT, userAgent);
		}

		return httpGet;
	}

	public HttpPost createHttpPost(String url, JSONObject params)
			throws UnsupportedEncodingException {

		HttpPost httpPost = new HttpPost(url);
		if (userAgent != null) {
			httpPost.addHeader(USER_AGENT, userAgent);
		}

		if (params != null) {
			StringEntity entity = new StringEntity(params.toString(),
					HTTP.UTF_8);
			httpPost.setEntity(entity);
		}

		return httpPost;
	}

	public String post(String url, JSONObject params) {
		try {
			HttpPost httpPost = createHttpPost(url, params);
			return doHttpRequest(httpPost);

		} catch (Exception e) {
			Log.e(TAG, "doHttpRequest exception: " + e.toString());
		}

		return null;
	}

	public String get(String url, JSONObject params) {
		try {
			HttpGet httpGet = createHttpGet(url, params);
			return doHttpRequest(httpGet);

		} catch (Exception e) {
			Log.e("version", "doHttpRequest exception: " + e.toString());
		}

		return null;
	}

	public String doHttpRequest(HttpRequestBase httpRequest) {
		if (debug) {
			Log.d(TAG, "doHttpRequest: " + httpRequest.getURI());
		}

		try {
			// httpRequest.addHeader("Accept-Encoding", "gzip");
			this.httpClient.getConnectionManager().closeExpiredConnections();
			long start = System.currentTimeMillis();
			HttpResponse response = this.httpClient.execute(httpRequest);

			long end = System.currentTimeMillis();
			if (debug) {
				Log.d(TAG, "doHttpRequest time = " + (end - start) + "    url="
						+ httpRequest.getURI());
			}
			int statusCode = response.getStatusLine().getStatusCode();
			switch (statusCode) {
			case 200:
				StringBuilder builder = new StringBuilder();
				builder.append(EntityUtils.toString(response.getEntity(),
						HTTP.UTF_8));

				/*
				 * InputStream is = getUngzippedContent(response.getEntity());
				 * BufferedReader reader=new BufferedReader(new
				 * InputStreamReader(is)); String line = reader.readLine();
				 * 
				 * while (line != null) { builder.append(line); line =
				 * reader.readLine(); } is.close();
				 */

				Header etagHeader = response.getFirstHeader("Etag");
				if (etagHeader != null) {
					int index = builder.indexOf("{");
					if (index != -1) {
						String etag = etagHeader.getValue();
						builder.insert(index + 1, "\"etag\":" + etag + ",");
					}
				}
				return builder.toString();
			default:
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					entity.consumeContent();
				}

				StringBuilder sb = new StringBuilder(EntityUtils.toString(
						entity, HTTP.UTF_8));
				return sb.toString();
			}
		} catch (IOException e) {
			if (debug) {
				Log.e(TAG, "doHttpRequest exception: " + e.toString());
			}
			httpRequest.abort();
			return null;
		}
	}

}
