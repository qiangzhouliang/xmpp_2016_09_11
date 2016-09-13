package com.qzl.xmpp_2016_09_11;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.qzl.xmpp_2016_09_11.dbhelper.SmsOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.SmsProvider;

/**
 * Created by Qzl on 2016-09-13.
 */
public class TestSmsProvider extends AndroidTestCase {
    public void testInsert() {

        ContentValues values = new ContentValues();
        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, "billy@itheima.com");
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, "qzl@itheima.com");
        values.put(SmsOpenHelper.SmsTable.BODY, "你好呀！");
        values.put(SmsOpenHelper.SmsTable.STATUS, "offline");
        values.put(SmsOpenHelper.SmsTable.TYPE, "chat");
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, "qzl@itheima.com");

        getContext().getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    public void testDelete() {
        getContext().getContentResolver().delete(SmsProvider.URI_SMS, SmsOpenHelper.SmsTable.FROM_ACCOUNT + "=?",
                new String[]{"billy@itheima.com"});
    }

    public void testUpdate() {
        ContentValues values = new ContentValues();
        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, "billy@itheima.com");
        values.put(SmsOpenHelper.SmsTable.BODY, "你好呀!!!!!!!!!!！");
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, "qzl@itheima.com");
        getContext().getContentResolver().update(SmsProvider.URI_SMS, values, SmsOpenHelper.SmsTable.FROM_ACCOUNT+"=?",
                new String[]{"billy@itheima.com"});
    }

    public void testQuery() {
        Cursor cursor = getContext().getContentResolver().query(SmsProvider.URI_SMS, null, null, null, null);
        //得到所有的列
        int columCount = cursor.getColumnCount();
        while (cursor.moveToNext()){
            for (int i = 0; i < columCount; i++) {
                System.out.print(cursor.getString(i) + "  ");
            }
            System.out.println();
        }
    }
}
