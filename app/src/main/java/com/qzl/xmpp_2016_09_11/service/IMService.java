package com.qzl.xmpp_2016_09_11.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.qzl.xmpp_2016_09_11.dbhelper.ContactOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.ContactsProvider;
import com.qzl.xmpp_2016_09_11.utils.PinyinUtil;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * Created by Qzl on 2016-09-12.
 */
public class IMService extends Service {
    public static XMPPConnection conn;
    public static String mCurAccount;//当前登录用户的jid
    private MyRosterListener mMyRosterListener;
    private Roster mRoster;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //同步花名册
                /*------------------------同步花名册开始--------------------*/
                System.out.println("同步花名册开始");
                //需要连接对象
                //得到花名册对象
                mRoster = IMService.conn.getRoster();
                //得到所有联系人
                final Collection<RosterEntry> entries = mRoster.getEntries();
                mMyRosterListener = new MyRosterListener();
                //监听联系人的改变
                mRoster.addRosterListener(mMyRosterListener);
                for (RosterEntry entry : entries) {
                    saveOrUpdateEntry(entry);
                }
                System.out.println("同步花名册结束");
                /*--------------------同步花名册结束---------------------------*/
            }
        });
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //移除rosterListener
        if (mMyRosterListener != null && mRoster != null) {
            mRoster.removeRosterListener(mMyRosterListener);
        }
    }

    public class MyRosterListener implements RosterListener {

        //联系人添加了
        @Override
        public void entriesAdded(Collection<String> addresses) {
            System.out.println("----------------entriesAdded----------------");
            //对应更新数据库
            for (String address : addresses) {
                RosterEntry entry = mRoster.getEntry(address);
                //要么更新，要么插入
                saveOrUpdateEntry(entry);
            }
        }

        //联系人修改了
        @Override
        public void entriesUpdated(Collection<String> addresses) {
            System.out.println("----------------entriesUpdated----------------");
            for (String address : addresses) {
                RosterEntry entry = mRoster.getEntry(address);
                //要么更新，要么插入
                saveOrUpdateEntry(entry);
            }
        }

        //联系人删除了
        @Override
        public void entriesDeleted(Collection<String> addresses) {
            System.out.println("----------------entriesDeleted----------------");
            for (String account : addresses) {
                //执行删除操作
                getContentResolver().delete(ContactsProvider.URI_CONTACT,
                        ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
            }
        }

        //联系人状态改变了
        @Override
        public void presenceChanged(Presence presence) {
            System.out.println("----------------presenceChanged----------------");
        }
    }

    private void saveOrUpdateEntry(RosterEntry entry) {
        ContentValues values = new ContentValues();
        String account = entry.getUser();

//                    account = account.substring(0,account.indexOf("@"))+"@"+ LoginActivity.SERVICENAME;
        String nickName = entry.getName();
        //处理昵称为空的现象
        if (nickName == null || "".equals(nickName)) {
            nickName = account.substring(0, account.indexOf("@"));//裁剪 billy@itheima.com --> billy
        }
        values.put(ContactOpenHelper.ContactTable.ACCOUNT, account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME, nickName);
        values.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        values.put(ContactOpenHelper.ContactTable.PINGYIN, PinyinUtil.getPinyin(account));

        //先update，后插入--》重点
        int updateCount = getContentResolver().update(ContactsProvider.URI_CONTACT, values, ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
        if (updateCount <= 0) {//没有更新到任何记录
            getContentResolver().insert(ContactsProvider.URI_CONTACT, values);
        }
    }
}
