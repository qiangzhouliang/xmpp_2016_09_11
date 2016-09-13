package com.qzl.xmpp_2016_09_11;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.qzl.xmpp_2016_09_11.dbhelper.ContactOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.ContactsProvider;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by Qzl on 2016-09-12.
 */
public class TestContactProvider extends AndroidTestCase {
    public void testInsert(){

        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT,"qg");
        values.put(ContactOpenHelper.ContactTable.NICKNAME,"qzl");
        values.put(ContactOpenHelper.ContactTable.AVATAR,"图片");
        values.put(ContactOpenHelper.ContactTable.PINGYIN,"qzl");
        getContext().getContentResolver().insert(ContactsProvider.URI_CONTACT,values);
    }
    public void testDelete(){
        getContext().getContentResolver().delete(ContactsProvider.URI_CONTACT, ContactOpenHelper.ContactTable.ACCOUNT+"=?",new String[]{"qg"});
    }
    public void testUpdate(){
        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT,"qg");
        values.put(ContactOpenHelper.ContactTable.NICKNAME,"我是老大");
        values.put(ContactOpenHelper.ContactTable.AVATAR,"图片");
        values.put(ContactOpenHelper.ContactTable.PINGYIN,"woshilaoda");
        getContext().getContentResolver().update(ContactsProvider.URI_CONTACT,values, ContactOpenHelper.ContactTable.ACCOUNT+"=?",new String[]{"qg"});
    }
    public void testQuery(){
        Cursor c = getContext().getContentResolver().query(ContactsProvider.URI_CONTACT, null, null, null, null);
        //一共多少列
        int columnCount = c.getColumnCount();
        while (c.moveToNext()){
            for (int i = 0; i < columnCount; i++) {
                System.out.print(c.getString(i) + "　");
            }
            System.out.println("");
        }
    }

    public void testPinyin(){
        //String convertToPinyinString = PinyinHelper.convertToPinyinString("内容", "分割符", 拼音的格式);
        String convertToPinyinString = PinyinHelper.convertToPinyinString("我是一个程序员", "", PinyinFormat.WITHOUT_TONE);
        System.out.println(convertToPinyinString);

    }
}
