package com.bluefay.android;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageResource {

	public static final String SCHEME_CONTENT = "content://";
	public static final String SCHEME_ANDROID_RESOURCE = "android.resource://";
	public static final String SCHEME_FILE = "file://";
	public static final String SCHEME_HTTP = "http://";

	@Deprecated
	public static Bitmap getImage(Context context, String uri) {
		uri = uri.substring(SCHEME_ANDROID_RESOURCE.length());
		String[] list = uri.split("/");
		if (list.length != 2) {
			return null;
		}
		int id = context.getResources().getIdentifier(list[1], list[0],
				"com.snda.gk.wallpaper");
		return BitmapFactory.decodeResource(context.getResources(), id);
	}

	@Deprecated
	public static Bitmap getImage(Context context, String uri, int destw,
			int desth) {
		uri = uri.substring(SCHEME_ANDROID_RESOURCE.length());
		String[] list = uri.split("/");
		if (list.length != 2) {
			return null;
		}

		int id = context.getResources().getIdentifier(list[1], list[0],
				"com.snda.gk.wallpaper");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(context.getResources(), id, options);
		// int s = options.outWidth / destw;
		int s = options.outHeight / desth;
		if (s <= 0) {
			s = 1;
		}
		options.inSampleSize = s;
		options.inJustDecodeBounds = false;

		return BitmapFactory
				.decodeResource(context.getResources(), id, options);

	}

	@Deprecated
	public static InputStream getRawResource(Context context, String uri) {
		uri = uri.substring(SCHEME_ANDROID_RESOURCE.length());
		String[] list = uri.split("/");
		if (list.length != 2) {
			return null;
		}
		int id = context.getResources().getIdentifier(list[1], list[0],
				"com.snda.gk.wallpaper");
		return context.getResources().openRawResource(id);
	}

	public static InputStream getInputStream(Context context, String uri) {
		InputStream is = null;
		if (uri.startsWith(SCHEME_ANDROID_RESOURCE)) {
			try {
				is = context.getContentResolver().openInputStream(
						Uri.parse(uri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			if (uri.startsWith(SCHEME_FILE)) {
				uri = uri.substring(SCHEME_FILE.length());
			}
			if (uri.startsWith("/android_asset/")) {
				uri = uri.substring(15);
				try {
					is = context.getAssets().open(uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					is = new FileInputStream(uri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return is;
	}

	public static Bitmap getImageStandard(Context context, String uri) {
		InputStream is = null;
		Bitmap bm = null;
		try {
			is = context.getContentResolver().openInputStream(Uri.parse(uri));
			bm = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing here
				}
			}
		}
		return bm;
	}

	public static Bitmap getImageStandard(Context context, String uri,
			int destw, int desth) {
		InputStream is = null;
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			is = context.getContentResolver().openInputStream(Uri.parse(uri));
			BitmapFactory.decodeStream(is, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing here
				}
			}
		}

		// int s = options.outWidth / destw;
		int s = options.outHeight / desth;
		if (s <= 0) {
			s = 1;
		}
		options.inSampleSize = s;
		options.inJustDecodeBounds = false;
		try {
			is = context.getContentResolver().openInputStream(Uri.parse(uri));
			bm = BitmapFactory.decodeStream(is, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing here
				}
			}
		}
		return bm;

	}
}
