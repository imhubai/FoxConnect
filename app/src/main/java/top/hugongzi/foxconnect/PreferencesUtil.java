package top.hugongzi.foxconnect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtil {

    public static void setPre(Activity activity, final String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        prefs.edit().putString(key, value).apply();
    }

    public static void setPre(Activity activity, final String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        prefs.edit().putInt(key, value).apply();
    }

    public static void setPre(Activity activity, final String key, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        prefs.edit().putLong(key, value).apply();
    }

    public static void setPre(Activity activity, final String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        prefs.edit().putBoolean(key, value).apply();
    }

    public static String getPre(Activity activity, final String key, final String defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String value = prefs.getString(key, defaultValue);
        return value;
    }

    public static int getPre(Activity activity, final String key, final int defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int value = prefs.getInt(key, defaultValue);
        return value;
    }

    public static boolean getPre(Activity activity, final String key, final boolean defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean value = prefs.getBoolean(key, defaultValue);
        return value;
    }
}