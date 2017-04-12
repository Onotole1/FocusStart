package com.spitchenko.focusstart.controller;

import android.os.Build;

/**
 * Date: 12.04.17
 * Time: 11:14
 *
 * @author anatoliy
 */
public enum VersionAndroidComparator {
    ;

    public static boolean isAndroidOld() {
        switch (Build.VERSION.SDK_INT) {
            case 14:
                return true;
            case 15:
                return true;
            case 16:
                return true;
            case 17:
                return true;
            case 18:
                return true;
            case 19:
                return true;
            case 21:
                return true;
            case 22:
                return true;
            default:
                return false;
        }
    }
}
