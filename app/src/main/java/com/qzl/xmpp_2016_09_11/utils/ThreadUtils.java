package com.qzl.xmpp_2016_09_11.utils;

import android.os.Handler;

/**
 * Created by Qzl on 2016-09-11.
 */
public class ThreadUtils {
    /**子线程执行 task*/
    public static void runInThread(Runnable task){
        new Thread(task).start();
    }

    /**
     * 主线程里面的一个handler
     */
    public static Handler sHandler = new Handler();
    /**UI线程执行 task*/
    public static void runInUIThread(Runnable task){
        sHandler.post(task);
    }
}
