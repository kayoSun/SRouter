package com.kayo.srouter.api;

import android.content.Intent;

/**
 * Kayo
 * 2018/7/7
 */
public class Back {
    public static String TAG = Back.class.getSimpleName();

    public int requestCode;
    public int resultCode;
    public Intent data;

    public Back(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }
}
