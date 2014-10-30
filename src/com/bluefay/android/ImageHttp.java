package com.bluefay.android;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.bluefay.core.BLLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;

public class ImageHttp {

	public static Bitmap downloadBitmap(String url) {
		// final int IO_BUFFER_SIZE = 4 * 1024;

		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
					|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				Header[] headers = response.getHeaders("Location");
				if (headers != null && headers.length > 0) {
					String moveurl = headers[0]
							.getValue();
					return downloadBitmap(moveurl);
				}
			}
			if (statusCode != HttpStatus.SC_OK) {
				BLLog.e("Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}


			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					// return BitmapFactory.decodeStream(inputStream);
					// Bug on slow connections, fixed in future release.
					FilterInputStream fit = new FlushedInputStream(inputStream);
					return BitmapFactory.decodeStream(fit);
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			getRequest.abort();
			BLLog.e("I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			BLLog.e("Incorrect URL: " + url);
		} catch (Exception e) {
			getRequest.abort();
			BLLog.e("Error while retrieving bitmap from " + url, e);
		} finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}
		return null;
	}

	/*
	 * An InputStream that skips the exact number of bytes provided, unless it
	 * reaches EOF.
	 */
	static class FlushedInputStream extends FilterInputStream {

		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

}
