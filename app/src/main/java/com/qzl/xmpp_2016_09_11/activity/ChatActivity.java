package com.qzl.xmpp_2016_09_11.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qzl.xmpp_2016_09_11.R;
import com.qzl.xmpp_2016_09_11.dbhelper.SmsOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.SmsProvider;
import com.qzl.xmpp_2016_09_11.service.IMService;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;
import com.qzl.xmpp_2016_09_11.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    public static final String CLICKACCOUNT = "clickAccount";
    public static final String CLICKNICKNAME = "clickNickName";

    @InjectView(R.id.chat_title)
    TextView mChatTitle;
    @InjectView(R.id.chat_listView)
    ListView mChatListView;
    @InjectView(R.id.chat_edit_body)
    EditText mChatEditBody;
    @InjectView(R.id.chat_btn_send)
    Button mChatBtnSend;

    private String mClickAccount;
    private String mClickNickName;
    private CursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);
        init();
        initView();
        initData();
        initListener();
    }

    private void init() {
        mClickAccount = getIntent().getStringExtra(ChatActivity.CLICKACCOUNT);
        mClickNickName = getIntent().getStringExtra(ChatActivity.CLICKNICKNAME);
        registerContentObserver();
    }

    private void initView() {
        //设置title
        mChatTitle.setText("与" + mClickNickName + "聊天中");
    }

    private void initData() {
        setAdapterOrNotify();
    }

    private void setAdapterOrNotify() {
        //1 首先判断是否存在adapter
        if (mAdapter != null) {
            //刷新
            mAdapter.getCursor().requery();
            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS, null, null, null, null);//aes 升序  desc：降序
                //如果没有数据，直接返回
                if (cursor.getCount() < 1) {
                    return;
                }
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //CursorAdapter :getView-->newView-->bindView
                        //如果convertView == null的时候会调用--》返回根布局
                        //具体设置数据
                        mAdapter = new CursorAdapter(ChatActivity.this, cursor) {
                            //如果convertView == null的时候会调用--》返回根布局
                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                TextView tv = new TextView(getApplicationContext());
                                return tv;
                            }

                            //具体设置数据
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                TextView tv = (TextView) view;
                                tv.setText(body);
                            }
                        };
                        mChatListView.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

    private void initListener() {

    }

    @OnClick(R.id.chat_btn_send)
    public void send(View view) {
        final String body = mChatEditBody.getText().toString();
        Toast.makeText(ChatActivity.this, body, Toast.LENGTH_SHORT).show();
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //1 获取消息的管理者
                    ChatManager chatManager = IMService.conn.getChatManager();
                    // 2 创建聊天对象
                    //chatManager.createChat("被发送对象jid",“消息的监听者”);
                    MyMessageListener messageListener = new MyMessageListener();
                    Chat chat = chatManager.createChat(mClickAccount, messageListener);
                    //3 发送消息
                    Message msg = new Message();

                    msg.setFrom(IMService.mCurAccount);//当前登录的用户
                    msg.setTo(mClickAccount);//发送给谁
                    msg.setBody(body);//发送的内容
                    msg.setType(Message.Type.chat);//类型就是聊天
                    msg.setProperty("key", "value");//额外属性--》额外的信息，这里我们用不到

                    chat.sendMessage(msg);

                    //保存消息
                    saveMessage(mClickAccount, msg);

                    //4 清空输入框
                    ThreadUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mChatEditBody.setText("");
                        }
                    });
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 保存消息--》contentResolver——》contentProvider——》sqlite
     *
     * @param msg
     */
    private void saveMessage(String sessionAccpunt, Message msg) {
        ContentValues values = new ContentValues();
        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, msg.getFrom());
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, msg.getTo());
        values.put(SmsOpenHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsOpenHelper.SmsTable.STATUS, "offline");
        values.put(SmsOpenHelper.SmsTable.TYPE, msg.getType().name());
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, sessionAccpunt);

        getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    class MyMessageListener implements MessageListener {

        //处理消息
        @Override
        public void processMessage(Chat chat, Message message) {
            String body = message.getBody();
            ToastUtils.showToastSafe(ChatActivity.this, body);
            //收到消息，保存消息
            //得到一个参与者
            String participant = chat.getParticipant();
//            System.out.println("participant : " + participant);
            saveMessage(participant, message);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterContentObserver();
    }

    /**
     * ----------------------使用contentObserver监听记录的时刻改变--------------------------
     */
    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    //注册监听
    public void registerContentObserver() {
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mMyContentObserver);
    }

    //反注册监听
    public void unregisterContentObserver() {
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 接收到数据记录的改变
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //设置adapter或者更新
            setAdapterOrNotify();
            super.onChange(selfChange, uri);
        }
    }
}
