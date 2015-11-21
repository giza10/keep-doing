package com.hkb48.keepdo.com.hkb48.keepdo.util;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;

public class CompatUtil {
    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }
}
