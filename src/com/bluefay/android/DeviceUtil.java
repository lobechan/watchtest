package com.bluefay.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


public class DeviceUtil
{
    public static final String TAG = MiscUtil.getClassName(DeviceUtil.class);

    public enum PhoneProfile
    {
        SILENCE,
        RINGER_ONLY,
        VIBRATE_ONLY,
        RINGER_AND_VIBRATE,
    }

    public enum VolumeOp
    {
        MIN,
        LOWER,
        RAISE,
        MAX,
    }

    private DeviceUtil()
    {
    }

    public static int getScreenOrientation(Context ctx)
    {
        int orientation = Configuration.ORIENTATION_UNDEFINED;

        WindowManager manager = (WindowManager)(ctx.getSystemService(Context.WINDOW_SERVICE));
        Display display = manager.getDefaultDisplay();
        if (display.getWidth() < display.getHeight())
        {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        else if (display.getWidth() > display.getHeight())
        {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        else
        {
            orientation = Configuration.ORIENTATION_SQUARE;
        }

        return orientation;
    }

    public static void setScreenAutoRotation(Context ctx, boolean enabled)
    {
        ContentResolver resolver = ctx.getContentResolver();
        System.putInt(resolver, System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

    public static void setWifiEnabled(Context ctx, boolean enabled)
    {
        WifiManager manager = (WifiManager)(ctx.getSystemService(Context.WIFI_SERVICE));
        manager.setWifiEnabled(enabled);
    }

    public static void setFlightMode(Context ctx, boolean enabled)
    {
        ContentResolver resolver = ctx.getContentResolver();
        System.putInt(resolver, System.AIRPLANE_MODE_ON, enabled ? 1 : 0);

        // Post an intent to reload
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabled);
        ctx.sendBroadcast(intent);
    }

    public static void setBluetoothEnabled(boolean enabled)
    {
        if (enabled)
        {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
        else
        {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    public static void setGpsEnabled(Context ctx, boolean enabled)
    {
//        String provider = Secure.getString(ctx.getContentResolver(), Secure.LOCATION_PROVIDERS_ALLOWED);
//        boolean currentEnabled = provider.contains(LocationManager.GPS_PROVIDER);
//        if (enabled ^ currentEnabled)
//        {
//            // toggle GPS on/off
//            final Intent intent = new Intent();
//            intent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
//            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
//            intent.setData(Uri.parse("3"));
//            ctx.sendBroadcast(intent);
//        }
    	boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(ctx.getContentResolver(), LocationManager.GPS_PROVIDER);
    	if(enabled && !gpsEnabled){
    		 Settings.Secure.setLocationProviderEnabled(ctx.getContentResolver(), LocationManager.GPS_PROVIDER, true);
    	}else if(!enabled && gpsEnabled){
    		Settings.Secure.setLocationProviderEnabled(ctx.getContentResolver(), LocationManager.GPS_PROVIDER, false );
    	}
    }

    private static final boolean setMobileDataEnabled(Context ctx, boolean enabled)
    {
        boolean success = false;

        try
        {
            ConnectivityManager manager = (ConnectivityManager)(ctx.getSystemService(Context.CONNECTIVITY_SERVICE));
            MiscUtil.invokeSimpleMethod(manager, "setMobileDataEnabled", boolean.class, enabled);
            success = true;
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error setMobileDataEnabled: " + ex.getMessage());
            ex.printStackTrace();
        }

        return success;
    }

    public static final boolean setMobileNetworkEnabled(Context ctx, boolean enabled)
    {
        boolean success = setMobileDataEnabled(ctx, enabled);

        if (!success)
        {
            try
            {
                TelephonyManager manager = (TelephonyManager)(ctx.getSystemService(Context.TELEPHONY_SERVICE));
                int state = manager.getDataState();
                boolean currentEnabled = (TelephonyManager.DATA_CONNECTING == state || TelephonyManager.DATA_CONNECTED == state);
                //Log.d(TAG, "current mobile data connection is: " + currentEnabled);

                if (currentEnabled ^ enabled)
                {
                    //Log.d(TAG, "need to set mobile data connection to: " + enabled);
                    Object telephony = MiscUtil.invokeSimpleMethod(manager, "getITelephony");

                    if (enabled)
                    {
                        MiscUtil.invokeSimpleMethod(telephony, "enableApnType", String.class, "default");
                        MiscUtil.invokeSimpleMethod(telephony, "enableDataConnectivity");
                    }
                    else
                    {
                        MiscUtil.invokeSimpleMethod(telephony, "disableApnType", String.class, "default");
                        MiscUtil.invokeSimpleMethod(telephony, "disableDataConnectivity");
                    }
                }

                success = true;
            }
            catch (Exception ex)
            {
                //Log.e(TAG, "Error setMobileNetworkEnabled: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        return success;
    }

    public static final void setPhoneProfile(Context ctx, PhoneProfile profile)
    {
        AudioManager audio = (AudioManager)(ctx.getSystemService(Context.AUDIO_SERVICE));

        switch (profile)
        {
        case SILENCE:
            audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
            break;
        case RINGER_ONLY:
            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
            break;
        case VIBRATE_ONLY:
            audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            break;
        case RINGER_AND_VIBRATE:
            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
            audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            break;
        default:
            break;
        }
    }

    public static final void setVolume(Context ctx, VolumeOp action)
    {
        AudioManager mAudioManager = (AudioManager)(ctx.getSystemService(Context.AUDIO_SERVICE));

        switch (action)
        {
        case MIN:
		    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_PLAY_SOUND);
            break;
        case LOWER:
        	//mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_PLAY_SOUND);
        	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_PLAY_SOUND);
            break;
        case RAISE:
        	//mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_PLAY_SOUND);
        	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_PLAY_SOUND);
            break;
        case MAX:
        	int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_PLAY_SOUND);
            break;
        default:
            break;
        }
    }

    public static final void setScreenBrightness(Context ctx, VolumeOp op)
    {
        final int BRIGHTNESS_MIN = 20;
        final int BRIGHTNESS_STEP = 32;
        final int BRIGHTNESS_MAX = 255;

        try
        {
            ContentResolver resolver = ctx.getContentResolver();
            int currentBrightness = System.getInt(resolver, System.SCREEN_BRIGHTNESS);
            int brightness = currentBrightness;

            switch (op)
            {
            case MIN:
                brightness = BRIGHTNESS_MIN;
                break;
            case LOWER:
                brightness -= BRIGHTNESS_STEP;
                if(brightness<BRIGHTNESS_MIN){
                	brightness=BRIGHTNESS_MIN;
                }
                break;
            case RAISE:
                brightness += BRIGHTNESS_STEP;
                if(brightness>BRIGHTNESS_MAX){
                	brightness=BRIGHTNESS_MAX;
                }
                break;
            case MAX:
                brightness = BRIGHTNESS_MAX;
                break;
            default:
                break;
            }

            if (brightness != currentBrightness)
            {
                System.putInt(ctx.getContentResolver(), System.SCREEN_BRIGHTNESS, brightness);

//                Intent intent = new Intent(ctx, RefreshActivity.class);
//                // below line is important
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                // intent.putExtra("brightness value", brightness / 255.0f);
//                ctx.getApplicationContext().startActivity(intent);
               
                if(ctx instanceof Activity){
                	Log.d("debug", "ctx instanceof Activity...");
                	Activity activity=(Activity)ctx;
                	 WindowManager.LayoutParams lp = activity.getWindow().getAttributes(); 
        		     lp.screenBrightness = brightness/255.0f;
        		     activity.getWindow().setAttributes(lp); 
                }else{
                	//Log.d("debug", "ctx not instanceof Activity...");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
