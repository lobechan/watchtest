package com.bluefay.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class MiscUtil
{
    private static final String TAG = MiscUtil.getClassName(MiscUtil.class);

    @SuppressWarnings("unchecked")
    public static <T extends Object>T safeCast(Object obj, Class<T> clazz)
    {
        return (null != obj && clazz.isInstance(obj)) ? (T)obj : null;
    }

    public static boolean isNotEmpty(String str)
    {
        return (null != str && str.length() > 0);
    }

    public static boolean isEmpty(String str)
    {
        return (!isNotEmpty(str));
    }

    public static boolean isEqual(String a, String b)
    {
        return ((null == a) ? isEmpty(b) : a.equals(b));
    }

    public static boolean isEqualIgnoreCase(String a, String b)
    {
        return ((null == a) ? isEmpty(b) : a.equalsIgnoreCase(b));
    }

    public static String removePunctuation(String text)
    {
        return ((null == text) ? null : text.replaceAll("[\\p{P}\\p{S}]", ""));
    }

    public static void runOnUiThread(Runnable action)
    {
        Looper mainLooper = Looper.getMainLooper();
        if (Thread.currentThread() == mainLooper.getThread())
        {
            // already UI thread, run it directly
            action.run();
        }
        else
        {
            // post the action on UI thread
            postOnUiThread(action);
        }
    }

    public static void postOnUiThread(Runnable action, long delayMillis)
    {
        if (delayMillis > 0)
        {
            new Handler(Looper.getMainLooper()).postDelayed(action, delayMillis);
        }
        else
        {
            postOnUiThread(action);
        }
    }

    public static void postOnUiThread(Runnable action)
    {
        new Handler(Looper.getMainLooper()).post(action);
    }

    public static boolean isSystemApp(PackageInfo pak)
    {
        return ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
    }

    public static int getVersionCode(Context ctx)
    {
        int versionCode = -1;

        PackageManager pm = ctx.getPackageManager();
        try
        {
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);
            versionCode = info.versionCode;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return versionCode;
    }
    
    public static String getVersionName(Context ctx)
    {
        String versionName = null;

        PackageManager pm = ctx.getPackageManager();
        try
        {
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);
            versionName = info.versionName;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return versionName;
    }

    private static boolean startAppInRawWay(Context ctx, String packageName)
    {
        boolean success = false;

        try
        {
            PackageManager pm = ctx.getPackageManager();

            // First see if the package has an INFO activity; the existence of
            // such an activity is implied to be the desired front-door for the
            // overall package (such as if it has multiple launcher entries).
            Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
            intentToResolve.addCategory(Intent.CATEGORY_INFO);
            intentToResolve.setPackage(packageName);
            List<ResolveInfo> ris = pm.queryIntentActivities(intentToResolve, 0);

            // Otherwise, try to find a main launcher activity.
            if (null == ris || ris.size() <= 0)
            {
                // reuse the intent instance
                intentToResolve.removeCategory(Intent.CATEGORY_INFO);
                intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
                intentToResolve.setPackage(packageName);
                ris = pm.queryIntentActivities(intentToResolve, 0);
            }

            if (null != ris && ris.size() > 0)
            {
                Intent intent = new Intent(intentToResolve);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);

                ctx.startActivity(intent);
                success = true;
            }
        }
        catch (Exception ex)
        {
            Log.w(TAG, "Can't launch package in raw way: " + packageName);
        }

        return success;
    }

    public static boolean startApp(Context ctx, String packageName)
    {
        boolean success = false;

        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
        if (null != intent)
        {
            try
            {
                ctx.startActivity(intent);
                success = true;
            }
            catch (Exception ex)
            {
                success = startAppInRawWay(ctx, packageName);
            }
        }

        return success;
    }
    
    public static boolean uninstallApp(Context ctx,String packageName){
    	try {
    		Uri uri=Uri.parse("package:"+packageName);
        	Intent intent=new Intent(Intent.ACTION_DELETE, uri);
        	ctx.startActivity(intent);
        	return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
    	
    }

    public static boolean startApp(Context ctx, PackageInfo pak)
    {
        return startApp(ctx, pak.packageName);
    }

    public static void showText(Context ctx, String text)
    {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
    }

    public static void showText(Context ctx, int resId)
    {
        Toast.makeText(ctx, resId, Toast.LENGTH_SHORT).show();
    }

    public static String getFileSize(double size)
    {
        final int SIZE_UNIT = 1024;
        final int UNIT_INDEX_MAX = 4;
        final String UNIT_SUBFIX[] = {"B", "KB", "MB", "GB", "TB"};

        int pos = 0;
        while (size >= SIZE_UNIT && pos < UNIT_INDEX_MAX)
        {
            size /= SIZE_UNIT;
            ++pos;
        }

        return String.format("%.2f%s", size, UNIT_SUBFIX[pos]);
    }

    private static String getExtension(File file)
    {
        return getExtension(file.getName());
    }

    public static String getExtension(String name)
    {
        String ext = null;

        int pos = 1 + name.lastIndexOf('.');
        if (pos > 0 && pos < name.length())
        {
            ext = name.substring(pos, name.length()).toLowerCase();
        }

        return ext;
    }

    public static String getClassName(Class<?> clazz)
    {
        return clazz.getSimpleName();
    }

    public static String getIekPrefix(Class<?> clazz)
    {
        return clazz.getName() + ".";
    }


    public static final Object invokeSimpleMethod(Object target, String methodName)
    {
        Method method = ReflectionUtils.findMethod(target.getClass(), methodName);
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, target);
    }

    public static final Object invokeSimpleMethod(Object target, String methodName, Class<?> paramType, Object paramValue)
    {
        Method method = ReflectionUtils.findMethod(target.getClass(), methodName, paramType);
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, target, paramValue);
    }

    public static class DownloadTaskBase extends AsyncTask<String, Integer, Boolean> implements ProgressCallback
    {
        private int _percentage = 0;

        @Override
        protected Boolean doInBackground(String... params)
        {
            Boolean success = false;
            String url = ((params.length > 0) ? params[0] : null);
            String localFilePath = ((params.length > 1) ? params[1] : null);

            if (isNotEmpty(url) && isNotEmpty(localFilePath))
            {
                try
                {
                    _percentage = 0;
                    success = downloadFile(url, localFilePath, this);
                }
                catch (ClientProtocolException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            return success;
        }

        @Override
        public void setProgress(long num, long max)
        {
            int percentage = (int)((max > 0) ? (100 * num / max) : 0);
            if (percentage > _percentage)
            {
                _percentage = percentage;
                this.publishProgress(new Integer[]{_percentage});
            }
        }
    }

    private interface ProgressCallback
    {
        void setProgress(long num, long max);
    }

    private static boolean downloadFile(String url, String localFilePath, DownloadTaskBase callback)
            throws ClientProtocolException, IOException
    {
        boolean success = false;

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);

        if (200 == response.getStatusLine().getStatusCode())
        {
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                File storeFile = new File(localFilePath);
                File dir = storeFile.getParentFile();
                if (!dir.exists())
                {
                    dir.mkdirs();
                }

                FileOutputStream output = new FileOutputStream(storeFile);
                InputStream input = entity.getContent();
                byte[] b = new byte[1024];
                long num = 0;
                long max = entity.getContentLength();
                for (int j = 0; ((j = input.read(b)) != -1);)
                {
                    if (callback.isCancelled())
                    {
                        //Log.i("DownloadTaskBase", "Task Killed");
                        return false;
                    }
                    else
                    {
                        output.write(b, 0, j);
                        num += j;
                        callback.setProgress(num, max);
                    }
                }

                callback.setProgress(max, max);
                output.flush();
                output.close();
                entity.consumeContent();
            }

            success = true;
        }

        return success;
    }
}
