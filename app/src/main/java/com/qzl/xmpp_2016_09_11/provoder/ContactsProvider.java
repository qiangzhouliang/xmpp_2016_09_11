package com.qzl.xmpp_2016_09_11.provoder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.qzl.xmpp_2016_09_11.dbhelper.ContactOpenHelper;

/**
 * 联系人的proVider
 * Created by Qzl on 2016-09-12.
 */
public class ContactsProvider extends ContentProvider {

    //主机地址常量--》当前类的完整路径
    public static final String AUTHORITIES = ContactsProvider.class.getCanonicalName();//得到一个类的完整路径
    //地址匹配对象
    static UriMatcher sUriMatcher;

    //对应联系人表的一个uri常量
    public static Uri URI_CONTACT = Uri.parse("content://" + AUTHORITIES + "/contact");
    public static final int CONTACT = 1;

    //刚进来就创建
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);//表示没有匹配的
        //添加一个匹配规则
        sUriMatcher.addURI(AUTHORITIES, "/contact", CONTACT);
        //content://com.qzl.xmpp_2016_09_11.provoder.ContactsProvider/contact-->CONTACT
    }

    private ContactOpenHelper mHelper;
    @Override
    public boolean onCreate() {
        mHelper = new ContactOpenHelper(getContext());
        if (mHelper != null) {
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /*--------------------------crud begin-----------------------------------*/
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //数据是存在sqlite--》创建数据库，建立表--》sqliteOpenHelper
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //新插入的id
                long id = db.insert(ContactOpenHelper.T_CONTACT, "", values);
                if (id != -1){
                    System.out.println("插入成功");
                    //拼接最新的uri
                    // content://com.qzl.xmpp_2016_09_11.provoder.ContactsProvider/contact/id
                    uri = ContentUris.withAppendedId(uri, id);
                    //通知observe数据改变了
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);//为空就是所有都能收到
                }
                break;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletecount = 0;
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //影响的行数
                deletecount =  db.delete(ContactOpenHelper.T_CONTACT, selection, selectionArgs);
                if (deletecount > 0){
                    System.out.println("删除成功");
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);
                }
                break;
        }
        return deletecount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                //更新的记录总数
                updateCount = db.update(ContactOpenHelper.T_CONTACT, values, selection, selectionArgs);
                if (updateCount > 0){
                    System.out.println("更新成功");
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);
                }
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        int code = sUriMatcher.match(uri);
        switch (code){
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                cursor = db.query(ContactOpenHelper.T_CONTACT, projection, selection, selectionArgs, null, null, sortOrder);
                System.out.println("查寻成功");
                break;
        }
        return cursor;
    }
/*--------------------------crud end-----------------------------------*/
}
