package com.qzl.xmpp_2016_09_11.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qzl.xmpp_2016_09_11.R;
import com.qzl.xmpp_2016_09_11.dbhelper.SmsOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.SmsProvider;
import com.qzl.xmpp_2016_09_11.service.IMService;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;

import org.jivesoftware.smack.packet.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private IMService mImService;

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
        registerContentObserver();
        //绑定服务
        Intent service = new Intent(ChatActivity.this, IMService.class);
        //如果法务创建了，我就绑定，如果服务还没有创建，我就创建
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
        mClickAccount = getIntent().getStringExtra(ChatActivity.CLICKACCOUNT);
        mClickNickName = getIntent().getStringExtra(ChatActivity.CLICKNICKNAME);

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
            Cursor cursor = mAdapter.getCursor();
            cursor.requery();
            mChatListView.setSelection(cursor.getCount() - 1);
            return;
        }
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS,//
                        null,//
                        "(from_account = ? and to_account=?)or(from_account = ? and to_account=?)",// where条件
                        new String[]{IMService.mCurAccount, mClickAccount, mClickAccount, IMService.mCurAccount},// where条件的参数
                        SmsOpenHelper.SmsTable.TIME + " ASC"// 根据时间升序排序
                );
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
                            public static final int RECEIVER = 0;
                            public static final int SECD = 1;
                            //如果convertView == null的时候会调用--》返回根布局
                            /*@Override
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
                            }*/

                            @Override
                            public int getItemViewType(int position) {
                                cursor.moveToPosition(position);
                                //取出消息的创建者
                                String fromAccount = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
                                if (!IMService.mCurAccount.equals(fromAccount)) {//接受
                                    return RECEIVER;
                                } else {//f发送
                                    return SECD;
                                }
                                //接受 --》如果当前的账号  不等于  消息的创建者
                                //发送
//                                return super.getItemViewType(position);// 0  1
                            }

                            @Override
                            public int getViewTypeCount() {
                                return super.getViewTypeCount() + 1;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {

                                ViewHolder holder;
                                if (getItemViewType(position) == RECEIVER) {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_receive, null);
                                        holder = new ViewHolder();
                                        convertView.setTag(holder);
                                        //holder赋值
                                        holder.time = (TextView) convertView.findViewById(R.id.time);
                                        holder.body = (TextView) convertView.findViewById(R.id.content);
                                        holder.head = (ImageView) convertView.findViewById(R.id.head);

                                    } else {
                                        holder = (ViewHolder) convertView.getTag();
                                    }

                                    //得到数据 展示数据
                                } else {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_send, null);
                                        holder = new ViewHolder();
                                        convertView.setTag(holder);
                                        //holder赋值
                                        holder.time = (TextView) convertView.findViewById(R.id.time);
                                        holder.body = (TextView) convertView.findViewById(R.id.content);
                                        holder.head = (ImageView) convertView.findViewById(R.id.head);
                                    } else {
                                        holder = (ViewHolder) convertView.getTag();
                                    }

                                    //得到数据 展示数据
                                }
                                //得到数据 展示数据
                                cursor.moveToPosition(position);
                                String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String time = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TIME));

                                String formTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time)));
                                holder.body.setText(body);
                                holder.time.setText(formTime);
                                return super.getView(position, convertView, parent);
                            }

                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                return null;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {

                            }

                            class ViewHolder {
                                TextView body;
                                TextView time;
                                ImageView head;
                            }
                        };
                        mChatListView.setAdapter(mAdapter);
                        //滚动到最后一行
                        mChatListView.setSelection(mAdapter.getCount() - 1);
                    }
                });
            }
        });
    }

    private void initListener() {

    }

    @OnClick(R.id.chat_btn_send)
    public void send(View view) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final String body = mChatEditBody.getText().toString();
                // 3 初始化了一个消息
                Message msg = new Message();
                msg.setFrom(IMService.mCurAccount);//当前登录的用户
                msg.setTo(mClickAccount);//发送给谁
                msg.setBody(body);//发送的内容
                msg.setType(Message.Type.chat);//类型就是聊天
                msg.setProperty("key", "value");//额外属性--》额外的信息，这里我们用不到

                // TODO: 2016-09-13 调运服务器里面的sendMessage方法
                mImService.sendMessage(msg);
                //4 清空输入框
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatEditBody.setText("");
                    }
                });
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterContentObserver();
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
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

    /**
     * 只有绑定服务，才能走这个方法
     */
    MyServiceConnection mMyServiceConnection = new MyServiceConnection();

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("---------------onServiceConnected------------------");
            IMService.MyBinder myBinder = (IMService.MyBinder) service;
            mImService = myBinder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("---------------onServiceDisconnected------------------");
        }
    }
}
