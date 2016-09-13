package com.qzl.xmpp_2016_09_11.provoder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.qzl.xmpp_2016_09_11.dbhelper.SmsOpenHelper;

/**
 * Created by Qzl on 2016-09-13.
 */
public class SmsProvider extends ContentProvider {

    private static final String AUTHORITIES = SmsProvider.class.getCanonicalName();//主机名称

    static UriMatcher sUriMatcher ;
    public static Uri URI_SMS = Uri.parse("content://" + AUTHORITIES + "/sms");
    public static final int SMS = 1;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //添加匹配规则
        sUriMatcher.addURI(AUTHORITIES,"/sms", SMS);
    }

    private SmsOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        //创建表，创建数据库
        mHelper = new SmsOpenHelper(getContext());
        if (mHelper != null){
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)){
            case SMS:
                long id = mHelper.getWritableDatabase().insert(SmsOpenHelper.T_SMS, "", values);
                if (id > 0){
                    System.out.println("SmsProvider 插入成功");
                    uri = ContentUris.withAppendedId(uri,id);
                    //发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;
        switch (sUriMatcher.match(uri)){
            case SMS:
                //具体删除了几条数据
                deleteCount = mHelper.getWritableDatabase().delete(SmsOpenHelper.T_SMS, selection, selectionArgs);
                if (deleteCount > 0){
                    System.out.println("SmsProvider 删除成功");
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (sUriMatcher.match(uri)){
            case SMS:
                updateCount = mHelper.getWritableDatabase().update(SmsOpenHelper.T_SMS, values, selection, selectionArgs);
                if (updateCount > 0){
                    System.out.println("SmsProvider 更新成功");
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        switch (sUriMatcher.match(uri)){
            case SMS:
                c = mHelper.getWritableDatabase().query(SmsOpenHelper.T_SMS, projection, selection, selectionArgs, null, null, sortOrder);
                System.out.println("SmsProvider 查寻成功");
                break;
        }
        return c;
    }
}