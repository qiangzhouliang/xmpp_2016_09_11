package com.qzl.xmpp_2016_09_11.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Qzl on 2016-09-11.
 */
public class ToastUtils {
    /**
     * 可以在子线程中探出toast
     * @param context
     * @param text
     */
    public static void showToastSafe(final Context context, final String text){
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
