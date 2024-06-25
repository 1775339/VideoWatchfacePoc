package com.titan.titanvideotrimmingpoc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * 常用工具类
 */
public class SM {

    public static final String no_value = "no_value";

    /**
     * 缓存sp-字符串
     */
    public static void spSaveString(Context context, String key, String value) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 读取sp-字符串
     */
    public static String spLoadString(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(key, no_value);
    }

    /**
     * 读取sp-字符串
     */
    public static String spLoadStringno(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(key, "");
    }


    /**
     * 缓存sp-布尔值
     */
    public static void spSaveBoolean(Context context, String key, boolean value) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 读取sp-布尔值
     */
    public static boolean spLoadBoolean(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getBoolean(key, false);
    }

    /**
     * 读取sp-布尔值
     */
    public static boolean spLoadBoolean(Context context, String key, boolean defValue) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getBoolean(key, defValue);
    }

    /**
     * 读取sp-布尔值（默认值为true）
     */
    public static boolean spLoadBooleanWithTrue(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getBoolean(key, true);
    }

    /**
     * 读取sp long
     */
    public static long spLoadLong(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getLong(key, 0);
    }


    /**
     * 缓存sp-long
     */
    public static void spSaveLong(Context context, String key, long value) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * 读取sp float
     */
    public static float spLoadFloat(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getFloat(key, 0.0f);
    }


    /**
     * 缓存sp-float
     */
    public static void spSaveFloat(Context context, String key, float value) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    /**
     * 读取sp int
     */
    public static int spLoadInt(Context context, String key) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getInt(key, 0);
    }


    /**
     * 缓存sp-int
     */
    public static void spSaveInt(Context context, String key, int value) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void saveObject(Context context, String object) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyPrefFile", Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.putString("object", object);
        editor.apply();
    }

    public static String readObject(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyPrefFile", Context.MODE_PRIVATE);
        return sharedPref.getString("object", null);
    }
}
