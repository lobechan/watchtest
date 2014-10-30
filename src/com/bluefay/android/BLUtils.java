package com.bluefay.android;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.json.JSONObject;

import com.bluefay.core.BLLog;
import com.bluefay.core.BLText;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

public class BLUtils {

	public static void show(Context context, String msg) {
		android.widget.Toast.makeText(context, msg,
				android.widget.Toast.LENGTH_SHORT).show();
	}

	public static void show(Context context, int msg) {
		android.widget.Toast.makeText(context, msg,
				android.widget.Toast.LENGTH_SHORT).show();
	}

	public static void show(String msg) {
		android.widget.Toast.makeText(MessageApplication.getAppContext(), msg,
				android.widget.Toast.LENGTH_SHORT).show();
	}

	public static void show(int msg) {
		android.widget.Toast.makeText(MessageApplication.getAppContext(), msg,
				android.widget.Toast.LENGTH_SHORT).show();
	}

	public static View create(Context context, int layout) {
		return LayoutInflater.from(context).inflate(layout, null);
	}

	public static View create(Context context, int layout, ViewGroup parent,
			boolean attachToRoot) {
		return LayoutInflater.from(context).inflate(layout, parent,
				attachToRoot);
	}

	public static Bitmap scaleBitmap(Context context, String location,
			int destw, int desth) {
		int schema = 0;
		if (location.startsWith("file://")) {
			location = location.substring(7);
		}
		if (location.startsWith("/android_asset/")) {
			location = location.substring(15);
			schema = 1;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		if (schema == 0) {
			BitmapFactory.decodeFile(location, options);
		} else {
			AssetManager asm = context.getAssets();
			try {
				InputStream is = asm.open(location);
				BitmapFactory.decodeStream(is, null, options);
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// int s = options.outWidth / destw;
		int s = options.outHeight / desth;
		if (s <= 0) {
			s = 1;
		}
		options.inSampleSize = s;
		options.inJustDecodeBounds = false;
		if (schema == 0) {
			return BitmapFactory.decodeFile(location, options);
		} else {
			AssetManager asm = context.getAssets();
			try {
				InputStream is = asm.open(location);
				return BitmapFactory.decodeStream(is, null, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Bitmap scale(Bitmap bp, float w, float h) {
		if (bp == null)
			return null;
		float bw = bp.getWidth();
		float bh = bp.getHeight();
		if (((int) w / bw == 1) && ((int) h / bh == 1))
			return bp;
		float sx = w / bw;
		float sy = h / bh;
		if (sx == 0)
			return null;
		Matrix matrix = new Matrix();
		matrix.postScale(sx, sy);
		// Bitmap returnBitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(),
		// bp.getHeight(), matrix, true);
		// BookReaderScrollPageLinearlayout.distoryBitmap(bp);
		// return returnBitmap;
		try {
			Bitmap returnBitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(),
					bp.getHeight(), matrix, true);
			if (bp != returnBitmap) {
				bp.recycle();
				bp = returnBitmap;
			}
		} catch (OutOfMemoryError ex) {
			// We have no memory to rotate. Return the original bitmap.
		}
		return bp;
	}

	public static String getStringValue(Context context, String key,
			String default_value) {
		SharedPreferences data = context.getSharedPreferences("config", 0);
		return data.getString(key, default_value);
	}

	public static boolean getBooleanValue(Context context, String key,
			boolean default_value) {
		SharedPreferences data = context.getSharedPreferences("config", 0);
		return data.getBoolean(key, default_value);
	}

	public static int getIntValue(Context context, String key, int default_value) {
		SharedPreferences data = context.getSharedPreferences("config", 0);
		return data.getInt(key, default_value);
	}

	public static long getLongValue(Context context, String key,
			long default_value) {
		SharedPreferences data = context.getSharedPreferences("config", 0);
		return data.getLong(key, default_value);
	}

	public static boolean setStringValue(Context context, String key,
			String value) {
		SharedPreferences.Editor data = context.getSharedPreferences("config",
				0).edit();
		data.putString(key, value);
		return data.commit();
	}

	public static boolean setBooleanValue(Context context, String key,
			boolean value) {
		SharedPreferences.Editor data = context.getSharedPreferences("config",
				0).edit();
		data.putBoolean(key, value);
		return data.commit();
	}

	public static boolean setIntValue(Context context, String key, int value) {
		SharedPreferences.Editor data = context.getSharedPreferences("config",
				0).edit();
		data.putInt(key, value);
		return data.commit();
	}

	public static boolean setLongValue(Context context, String key, long value) {
		SharedPreferences.Editor data = context.getSharedPreferences("config",
				0).edit();
		data.putLong(key, value);
		return data.commit();
	}

	public static Drawable createActivityIcon(Context context,
			String packageName, int icon) {
		if (packageName == null || icon == 0) {
			return null;
		}
		Context packageContext = null;
		try {
			packageContext = context.createPackageContext(packageName,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageContext != null) {
			return packageContext.getResources().getDrawable(icon);
		} else {
			return null;
		}
	}

	public static Bitmap createBitmapFromPath(Context context, String location) {
		// This method has issue to display bitmap size
		// Drawable bm = null;
		// if (location.startsWith("file://")) {
		// bm = Drawable.createFromPath(location.substring(7));
		// }
		if (context == null || location == null) {
			return null;
		}
		if (location.startsWith("file://")) {
			location = location.substring(7);
		}
		if (location.startsWith("/android_asset/")) {
			return createBitmapFromAsset(context, location.substring(15));
		}
		return BitmapFactory.decodeFile(location);

	}

	public static Drawable createDrawableFromPath(Context context,
			String location) {
		Bitmap bm = createBitmapFromPath(context, location);
		if (bm != null) {
			return new BitmapDrawable(context.getResources(), bm);
		} else {
			return null;
		}
	}

	public static void launcherBrowser(Context context, String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		if (url != null) {
			intent.setData(Uri.parse(url));
		}
		intent.setClassName("com.android.browser",
				"com.android.browser.BrowserActivity");
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			show(context, ex.getMessage());
		}
	}

	public static String getAppNameByPID(Context context, int pid) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		for (RunningAppProcessInfo processInfo : manager
				.getRunningAppProcesses()) {
			if (processInfo.pid == pid) {
				return processInfo.processName;
			}
		}
		return "";
	}

	public static String getSystemProperty(String key, String default_value) {

		Object res = invokeStaticMethod("android.os.SystemProperties", "get",
				key, default_value);
		if (res != null) {
			return (String) res;
		}
		return default_value;
	}

	public static Object invokeStaticMethod(String className,
			String methodName, Object... args) {

		Class<?> classObj = null;
		try {
			classObj = Class.forName(className);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		if (classObj == null) {
			return null;
		}

		if (args.length == 0) {
			try {
				Method method = classObj.getMethod(methodName);
				return method.invoke(null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else {
			Class<?>[] classargs = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				classargs[i] = args[i].getClass();
			}
			try {
				Method method = classObj.getMethod(methodName, classargs);
				return method.invoke(null, args);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Object invokeMethod(Object obj, String methodName,
			Object... args) {
		Class<?> classObj = obj.getClass();
		if (args.length == 0) {
			try {
				Method method = classObj.getMethod(methodName);
				return method.invoke(obj);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else {
			Class<?>[] classargs = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Boolean) {
					classargs[i] = Boolean.TYPE;
				} else if (args[i] instanceof Integer) {
					classargs[i] = Integer.TYPE;
				} else if (args[i] instanceof Long) {
					classargs[i] = Long.TYPE;
				} else {
					classargs[i] = args[i].getClass();
				}
			}
			try {
				Method method = classObj.getMethod(methodName, classargs);
				BLLog.d(method.toString());
				return method.invoke(obj, args);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Object invokeBooleanMethod(Object obj, String methodName,
			boolean value) {
		Class<?> classObj = obj.getClass();
		try {
			Method method = classObj.getMethod(methodName, Boolean.TYPE);
			return method.invoke(obj, value);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setSystemProperty(String key, String value) {

		invokeStaticMethod("android.os.SystemProperties", "set", key, value);
		BLLog.d("invokeMethod set OK");
	}

	public static Drawable createDrawableFromAsset(Context context, String name) {
		if (name == null || name.length() == 0) {
			return null;
		}
		AssetManager asm = context.getAssets();
		try {
			InputStream is = asm.open(name);
			Bitmap bm = BitmapFactory.decodeStream(is);
			if (bm != null) {
				return new BitmapDrawable(context.getResources(), bm);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap createBitmapFromAsset(Context context, String name) {
		if (name == null || name.length() == 0) {
			return null;
		}
		AssetManager asm = context.getAssets();
		try {
			InputStream is = asm.open(name);
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Context createPackageContext(Context context, String name) {
		Context res = null;
		try {
			res = context.createPackageContext(name,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static int getScreenDensityDpi(Context context) {
		return context.getResources().getDisplayMetrics().densityDpi;
	}

	public static int getScreenWidthPixels(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeightPixels(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		BLLog.d("scale = " + scale);
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		BLLog.d("scale = " + scale);
		return (int) (pxValue / scale + 0.5f);
	}

	public static String createStringFromAsset(Context context, String name) {
		AssetManager asm = context.getAssets();
		try {
			InputStream is = asm.open(name);
			return inputStream2String(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}

	public static String getPhoneIMEI(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			return tm.getDeviceId();
		} else {
			return "";
		}
	}

	public static String getWifiMac(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			WifiInfo info = wifi.getConnectionInfo();
			if (info != null) {
				String mac = info.getMacAddress();
				if (mac != null) {
					String ret = "";
					String ma[] = mac.split(":");
					for (int i = 0; i < ma.length; ++i) {
						ret += ma[i];
					}
					return ret;
				}
			}
		}
		
		return "";
	}
	public static String getWifiMac1(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			WifiInfo info = wifi.getConnectionInfo();
			String mac = info.getMacAddress();
			return mac;
		}
		
		return "";
	}
	public static boolean saveBitmapToFile(Bitmap bm, String filePath) {
		if (bm != null && filePath != null) {
			FileOutputStream output = null;
			CompressFormat format = Bitmap.CompressFormat.JPEG;
			if (filePath.endsWith(".png")) {
				format = Bitmap.CompressFormat.PNG;
			}
			if (filePath.startsWith("file://")) {
				filePath = filePath.substring(7);
			}
			try {
				output = new FileOutputStream(filePath);
				bm.compress(format, 100, output);
				output.close();
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static String[] listAsset(Context context, String packagename,
			String path) {
		Context packageContext = createPackageContext(context, packagename);
		AssetManager manager = packageContext.getAssets();
		try {
			return manager.list(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void closeSilently(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
			// do nothing
		}
	}

	public static void closeSilently(ParcelFileDescriptor c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
			// do nothing
		}
	}

	public static Bitmap convertViewToBitmap(View view, int bitmapWidth,
			int bitmapHeight) {
		Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
				Bitmap.Config.ARGB_8888);
		invokeBooleanMethod(bitmap, "setHasAlpha", false);
		view.draw(new Canvas(bitmap));
		return bitmap;
	}

	public static Drawable getPackageReource(Context context,
			String resource_name, String packageName) {
		Drawable drawable = null;
		try {
			Context customContext = context.createPackageContext(packageName,
					Context.CONTEXT_IGNORE_SECURITY);
			StringBuilder custrom_img_name = new StringBuilder(packageName);
			custrom_img_name.append(":drawable/").append(resource_name);
			int custromImageId = customContext.getResources().getIdentifier(
					custrom_img_name.toString(), null, null);
			drawable = customContext.getResources().getDrawable(custromImageId);

		} catch (Exception e) {

		}
		return drawable;
	}

	public static boolean isMobileNetwork(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null
				&& networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

	public static boolean isWifiNetwork(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null
				&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager conn = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = conn.getActiveNetworkInfo();
		if (net != null && net.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toHexString(byte[] array) {
		if (array == null) {
			return "";
		}
		return toHexString(array, 0, array.length);
	}

	public static String toHexString(byte[] array, int offset, int length) {
		char[] buf = new char[length * 2];
		int bufIndex = 0;
		for (int i = offset; i < offset + length; i++) {
			byte b = array[i];
			buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
			buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
		}
		return new String(buf);
	}

	public static String getMD5Str(String s) {
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes("UTF-8"));
			return toHexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Drawable zoomDrawable(Context context, Drawable drawable,
			int w, int h) {

		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return new BitmapDrawable(context.getResources(), newbmp);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			BLLog.e("Exception", e);
		}
		return versionName;
	}

	// forward SMS, maybe use Email, GMail or Messaging application which has
	// ACTION_SEND
	public static void forward(Context context, String body) {
		if (body != null) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, body);
			context.startActivity(intent);
		}
	}

	// forward SMS, only use Messaging
	public static void forwardBySms(Context context, String body) {
		if (body != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType("vnd.android-dir/mms-sms");
			// intent.putExtra("forwarded_message",true);
			// comment this may improve performance for forwarding sms
			intent.putExtra("sms_body", body);
			context.startActivity(intent);
		}
	}

	public static void forwardByMms(Context context, Uri uri) {

		// if (uri == null) {
		// return;
		// }
		//
		// PduPersister persister = PduPersister.getPduPersister(context);
		// GenericPdu pdu = null;
		// try {
		// pdu = persister.load(uri);
		// BLLog.d("Message Type:%d", pdu.getMessageType());
		// } catch (MmsException e1) {
		// e1.printStackTrace();
		// }
		// Uri copyuri = null;
		// String subject = null;
		// if (pdu != null
		// && pdu.getMessageType() == PduHeaders.MESSAGE_TYPE_SEND_REQ) {
		// SendReq sendReq = (SendReq) pdu;
		// EncodedStringValue encodestring = sendReq.getSubject();
		// if (encodestring != null) {
		// subject = encodestring.getString();
		// }
		//
		// } else if (pdu != null
		// && pdu.getMessageType() == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) {
		// RetrieveConf retrieve = (RetrieveConf) pdu;
		// EncodedStringValue encodestring = retrieve.getSubject();
		// if (encodestring != null) {
		// subject = encodestring.getString();
		// }
		// }
		// // Copy the parts of the message here.
		// try {
		// copyuri = persister.persist(pdu, Mms.Draft.CONTENT_URI);
		// } catch (MmsException e) {
		// BLLog.d("copy mms uri to draft exception");
		// e.printStackTrace();
		//
		// }
		//
		// Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setType("vnd.android-dir/mms-sms");
		// intent.putExtra("forwarded_message", true);
		// if (copyuri != null) {
		// intent.putExtra("msg_uri", copyuri);
		// }
		// if (subject != null) {
		// intent.putExtra("subject", subject);
		// }
		// context.startActivity(intent);
	}

	public static void newMessage(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setType("vnd.android-dir/mms-sms");
		// intent.putExtra("sms_body", "");
		context.startActivity(intent);
	}

	public static void newMessageNetwork(Context context) {
		Intent intent = new Intent(context, null);
		context.startActivity(intent);
	}

	public static void dial(Context context, String number) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + number));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
/*
	public static boolean isKeyguardLocked(Context context) {
		KeyguardManager mKeyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		return mKeyguardManager.isKeyguardLocked();
	}

	public static boolean isKeyguardSecure(Context context) {
		KeyguardManager mKeyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		return mKeyguardManager.isKeyguardSecure();
	}
*/
	public static KeyguardLock disableKeyguard(Context context) {
		KeyguardManager mKeyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("");
		mKeyguardLock.disableKeyguard();
		return mKeyguardLock;
	}

	public static void reenableKeyguard(Context context, KeyguardLock lock) {
		lock.reenableKeyguard();
	}

	// 字符序列转换为16进制字符串
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	/**
	 * Returns a screenshot of the current display contents.
	 */
	public static Bitmap createScreenshot(Context context) {
		// We need to orient the screenshot correctly (and the Surface api seems
		// to
		// take screenshots only in the natural orientation of the device :!)

		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		Display mDisplay = windowManager.getDefaultDisplay();

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		Matrix mDisplayMatrix = new Matrix();

		mDisplay.getMetrics(mDisplayMetrics);
		// boolean hasNavBar = context.getResources().getBoolean(
		// com.android.internal.R.bool.config_showNavigationBar);

		// boolean hasNavBar = WindowManagerService.hasSystemNavBar();
		boolean hasNavBar = false;

		float[] dims = { mDisplayMetrics.widthPixels,
				mDisplayMetrics.heightPixels };
		float degrees = getDegreesForRotation(mDisplay.getRotation());
		// final int statusBarHeight = context.getResources()
		// .getDimensionPixelSize(
		// com.android.internal.R.dimen.status_bar_height);
		//
		// // Navbar has different sizes, depending on orientation
		// final int navBarHeight = hasNavBar ? context.getResources()
		// .getDimensionPixelSize(
		// com.android.internal.R.dimen.navigation_bar_height) : 0;
		// final int navBarWidth = hasNavBar ? context.getResources()
		// .getDimensionPixelSize(
		// com.android.internal.R.dimen.navigation_bar_width) : 0;

		final int statusBarHeight = dip2px(context, 25);
		final int navBarHeight = hasNavBar ? dip2px(context, 48) : 0;
		final int navBarWidth = hasNavBar ? dip2px(context, 42) : 0;

		boolean requiresRotation = (degrees > 0);
		if (requiresRotation) {
			// Get the dimensions of the device in its native orientation
			mDisplayMatrix.reset();
			mDisplayMatrix.preRotate(-degrees);
			mDisplayMatrix.mapPoints(dims);
			dims[0] = Math.abs(dims[0]);
			dims[1] = Math.abs(dims[1]);
		}

		Object obj = invokeStaticMethod("android.view.Surface", "screenshot",
				(int) dims[0], (int) dims[1]);

		Bitmap bitmap = (Bitmap) obj; // Surface.screenshot((int) dims[0], (int)
										// dims[1]);
		// Bail if we couldn't take the screenshot
		if (bitmap == null) {
			return null;
		}

		if (requiresRotation) {
			// Rotate the screenshot to the current orientation
			Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels,
					mDisplayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(ss);
			c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
			c.rotate(360f - degrees);
			c.translate(-dims[0] / 2, -dims[1] / 2);
			c.drawBitmap(bitmap, 0, 0, null);

			bitmap = ss;
		}

		// TODO this is somewhat device-specific; need generic solution.
		// Crop off the status bar and the nav bar
		// Portrait: 0, statusBarHeight, width, height - status - nav
		// Landscape: 0, statusBarHeight, width - navBar, height - status
		int newLeft = 0;
		int newTop = statusBarHeight;
		int newWidth = bitmap.getWidth();
		int newHeight = bitmap.getHeight();
		if (bitmap.getWidth() < bitmap.getHeight()) {
			// Portrait mode: status bar is at the top, navbar bottom, width
			// unchanged
			newHeight = bitmap.getHeight() - statusBarHeight - navBarHeight;
		} else {
			// Landscape mode: status bar is at the top, navbar right
			newHeight = bitmap.getHeight() - statusBarHeight;
			newWidth = bitmap.getWidth() - navBarWidth;
		}
		bitmap = Bitmap.createBitmap(bitmap, newLeft, newTop, newWidth,
				newHeight);

		return bitmap;
	}

	/**
	 * @return the current display rotation in degrees
	 */
	public static float getDegreesForRotation(int value) {
		switch (value) {
		case Surface.ROTATION_90:
			return 90f;
		case Surface.ROTATION_180:
			return 180f;
		case Surface.ROTATION_270:
			return 270f;
		}
		return 0f;
	}

	public static int getAppVersionCode(Context c, String packageName) {
		int v = 0;
		PackageManager packageManager = c.getPackageManager();
		try {
			PackageInfo packInfo = packageManager
					.getPackageInfo(packageName, 0);
			v = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return v;
	}
	public static String getAppVersionName(Context c, String packageName) {
		String v = "";
		PackageManager packageManager = c.getPackageManager();
		try {
			PackageInfo packInfo = packageManager
					.getPackageInfo(packageName, 0);
			v = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return v;
	}
	public static String getFirmwareVersionName() {
		String version = (String) invokeStaticMethod(
				"android.os.SystemProperties", "get", "ro.build.display.id",
				"GS4.13000");
		return version;
	}

	public static String getFirmwareCustomVersionName() {
		String version = (String) invokeStaticMethod(
				"android.os.SystemProperties", "get", "ro.build.custom.id",
				"1.3.75");
		return version;
	}

	public static String getFirmwareVersionCode() {
		// if (MessageApplication.isDebugable()) {
		// return "13000";
		// } else {

		String version = (String) invokeStaticMethod(
				"android.os.SystemProperties", "get",
				"ro.build.version.incremental", "1");
		//return "0";
		return version;
		// }
	}

	public static String getHumanSize(long bytesize) {
		if (bytesize < 0) {
			return "0KB";
		}
		long KB = bytesize / 1024;
		if (KB < 1024) {
			return KB + "KB";
		}

		long MB = KB / 1024;
		if (MB < 1024) {
			return MB + "MB";
		}

		long GB = MB / 1024;
		if (GB < 1024) {
			return GB + "GB";
		}
		return "Large";
	}
	public static String getFromRaw(Context context,int resid){  
	    String result = "";  
	        try {  
	            InputStream in = context.getResources().openRawResource(resid);  
	            int lenght = in.available();  
	            byte[]  buffer = new byte[lenght];  
	            in.read(buffer);  
	            result = EncodingUtils.getString(buffer, "utf8");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        return result;  
	}
	public static String calcFileSha1(String path) {
		File file = new File(path);
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		MessageDigest messagedigest;
		try {
			messagedigest = MessageDigest.getInstance("SHA-1");

			byte[] buffer = new byte[1024 * 1024 * 2];
			int len = 0;

			while ((len = in.read(buffer)) > 0) {
				messagedigest.update(buffer, 0, len);
			}

			return byte2hex(messagedigest.digest());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String byte2hex(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs.append("0");
				hs.append(stmp);
			} else
				hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}
}
