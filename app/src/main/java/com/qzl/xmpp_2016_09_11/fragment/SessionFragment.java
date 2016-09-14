package com.qzl.xmpp_2016_09_11.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qzl.xmpp_2016_09_11.R;
import com.qzl.xmpp_2016_09_11.activity.ChatActivity;
import com.qzl.xmpp_2016_09_11.dbhelper.ContactOpenHelper;
import com.qzl.xmpp_2016_09_11.dbhelper.SmsOpenHelper;
import com.qzl.xmpp_2016_09_11.provoder.ContactsProvider;
import com.qzl.xmpp_2016_09_11.provoder.SmsProvider;
import com.qzl.xmpp_2016_09_11.service.IMService;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;

/**
 * 会话的fragment
 */
public class SessionFragment extends Fragment {

    private ListView mListView;
    private CursorAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        init();
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    private void init() {
        registerContentObserver();
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listView);
    }

    private void initData() {
        setOrNotifyAdapter();
    }

    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);//移动到指定位置
                //拿到jid（账号）--》 发送消息的时候需要
                String account = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                //拿到昵称——》显示效果
                String nickName = getNickNameByAccount(account);

                Intent intent = new Intent(getActivity(),ChatActivity.class);

                intent.putExtra(ChatActivity.CLICKACCOUNT,account);
                intent.putExtra(ChatActivity.CLICKNICKNAME,nickName);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        unRegisterContentObserver();
        super.onDestroy();
    }

    /**
     * 设置或者更新adapter
     */
    private void setOrNotifyAdapter() {
        //判断adapter是否存在
        if (mAdapter != null) {
            //刷新adapter
            mAdapter.getCursor().requery();
            return;
        }
        //开启线程，同步花名册
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //对应查寻记录
                final Cursor c = getActivity().getContentResolver().query(SmsProvider.URI_SESSION, null, null, new String[]{
                        IMService.mCurAccount, IMService.mCurAccount
                }, null);
                //假如没有数据的情况
                if (c.getCount() <= 0) {
                    return;
                }
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * BaseAdapter
                         *      /-CursorAdapter
                         */
                        //如果convertView == null,返回一个具体的视图
                        //设置数据显示数据
                        mAdapter = new CursorAdapter(getActivity(), c) {
                            //如果convertView == null,返回一个具体的视图
                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                View view = View.inflate(context, R.layout.item_session, null);
                                return view;
                            }

                            //设置数据显示数据
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                TextView tvBody = (TextView) view.findViewById(R.id.body);
                                TextView tvNickName = (TextView) view.findViewById(R.id.nickname);

                                String body = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String account = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                                //account 但是在聊天记录表（sms）里面没有保存别名信息，只有（Contact表里面才有）
                                String nickName = getNickNameByAccount(account);
                                tvBody.setText(body);
                                tvNickName.setText(nickName);
                            }
                        };
                        mListView.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

    //查找别名
    public String getNickNameByAccount(String account) {
        String nickName = "";
        Cursor c = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account}, null);
        if (c.getCount() > 0){
            //有数据
            c.moveToFirst();
            nickName = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
        }
        return nickName;
    }

    /*================================监听数据库记录的改变======================================*/
    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mMyContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unRegisterContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 如果数据库发生改变会在这个方法收到通知
         *
         * @param selfChange
         * @param uri
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            //更新adapter或者刷新adapter
            setOrNotifyAdapter();
        }
    }
}
