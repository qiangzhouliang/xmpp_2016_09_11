package com.qzl.xmpp_2016_09_11.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.qzl.xmpp_2016_09_11.activity.LoginActivity;
import com.qzl.xmpp_2016_09_11.dbhelper.ContactOpenHelper;
import com.qzl.xmpp_2016_09_11.dbhelper.SmsOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.ContactsProvider;
import com.qzl.xmpp_2016_09_11.provoder.SmsProvider;
import com.qzl.xmpp_2016_09_11.utils.PinyinUtil;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;
import com.qzl.xmpp_2016_09_11.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Qzl on 2016-09-12.
 */
public class IMService extends Service {
    public static XMPPConnection conn;
    public static String mCurAccount;//当前登录用户的jid
    private MyRosterListener mMyRosterListener;
    private Roster mRoster;

    private ChatManager mChatManager;
    private Chat mCurrentChat;

    private Map<String, Chat> mChatMap = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        /**
         * 返回service的实例
         */
        public IMService getService() {
            return IMService.this;
        }
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


                /*--------------------创建消息的管理者  注册监听 开始---------------------------*/
                //1 获取消息的管理者
                if (mChatManager == null) {
                    mChatManager = IMService.conn.getChatManager();
                }
                mChatManager.addChatListener(mMyChatManagerListener);
                /*--------------------创建消息的管理者  注册监听 结束--------------------------*/
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
        //移除消息监听
        if (mCurrentChat != null && mMyMessageListener != null) {
            mCurrentChat.removeMessageListener(mMyMessageListener);
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

    MyMessageListener mMyMessageListener = new MyMessageListener();

    class MyMessageListener implements MessageListener {

        //处理消息
        @Override
        public void processMessage(Chat chat, Message message) {
            String body = message.getBody();
            ToastUtils.showToastSafe(getApplicationContext(), body);
            //收到消息，保存消息
            //得到一个参与者
            String participant = chat.getParticipant();
//            System.out.println("participant : " + participant);
            saveMessage(participant, message);
        }
    }

    MyChatManagerListener mMyChatManagerListener = new MyChatManagerListener();

    class MyChatManagerListener implements ChatManagerListener {
        //会话的创建者
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {

            //判断chat是否存在在map里面
            String participant = chat.getParticipant();//得到参与者，和我哦聊天的那个人
            //因为别人创建和我自己创建，参与者（和我聊天的人）对应的jid不同，所以，需要统一处理
            participant = filterAccount(participant);
            if (!mChatMap.containsKey(participant)) {
                //保存chat
                mChatMap.put(participant, chat);
                chat.addMessageListener(mMyMessageListener);
            }
            if (createdLocally) {
                //我创建了一个chat
                System.out.println("我创建了一个chat");
            } else {
                //别人创建了一个chat
                System.out.println("别人创建了一个chat");
            }
        }
    }

    private void saveOrUpdateEntry(RosterEntry entry) {
        ContentValues values = new ContentValues();
        String account = entry.getUser();

        account = filterAccount(account);
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

    /**
     * 发送消息
     */
    public void sendMessage(final Message msg) {
        try {
            // 2 创建聊天对象
            //chatManager.createChat("被发送对象jid",“消息的监听者”);

            //判断chat对象是否已经创建
            String toAccount = msg.getTo();
            if (mChatMap.containsKey(toAccount)) {
                mCurrentChat = mChatMap.get(toAccount);

            } else {
                mCurrentChat = mChatManager.createChat(toAccount, mMyMessageListener);
                mChatMap.put(toAccount, mCurrentChat);
            }
            //发送消息
            mCurrentChat.sendMessage(msg);
            //保存消息
            saveMessage(msg.getTo(), msg);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存消息--》contentResolver——》contentProvider——》sqlite
     *
     * @param msg
     */
    private void saveMessage(String sessionAccpunt, Message msg) {
        ContentValues values = new ContentValues();
        sessionAccpunt = filterAccount(sessionAccpunt);
        //account = account.substring(0,account.indexOf("@"))+"@"+ LoginActivity.SERVICENAME;
        String from_account = msg.getFrom();
        from_account = filterAccount(from_account);
        String to_account = msg.getTo();
        to_account = filterAccount(to_account);

        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, from_account);
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, to_account);
        values.put(SmsOpenHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsOpenHelper.SmsTable.STATUS, "offline");
        values.put(SmsOpenHelper.SmsTable.TYPE, msg.getType().name());
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, sessionAccpunt);

        getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    //进行过滤
    @NonNull
    private String filterAccount(String sessionAccpunt) {
        return sessionAccpunt.substring(0, sessionAccpunt.indexOf("@")) + "@" + LoginActivity.SERVICENAME;
    }
}
