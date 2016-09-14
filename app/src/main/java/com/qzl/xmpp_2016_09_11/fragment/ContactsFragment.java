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
import com.qzl.xmpp_2016_09_11.provoder.ContactsProvider;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;

/**
 * 联系人的fragment
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    private ListView mListView;
    private CursorAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
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

    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);//移动到指定位置
                //拿到jid（账号）--》 发送消息的时候需要
                String account = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                //拿到昵称——》显示效果
                String nickName = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));

                Intent intent = new Intent(getActivity(),ChatActivity.class);

                intent.putExtra(ChatActivity.CLICKACCOUNT,account);
                intent.putExtra(ChatActivity.CLICKNICKNAME,nickName);
                startActivity(intent);
            }
        });
    }

    private void initData() {

        //设置adapter，显示数据
        setOrNotifyAdapter();
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
                final Cursor c = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null, null, null, null);
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
                                View view = View.inflate(context, R.layout.item_contact, null);
                                return view;
                            }

                            //设置数据显示数据
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                TextView tvAccount = (TextView) view.findViewById(R.id.account);
                                TextView tvNickName = (TextView) view.findViewById(R.id.nickname);

                                String account = cursor.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                                String nickName = cursor.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
                                tvAccount.setText(account);
                                tvNickName.setText(nickName);
                            }
                        };
                        mListView.setAdapter(mAdapter);
                    }
            });
        }
    });
}

    @Override
    public void onDestroy() {
        //按照常理，我们fragment销毁了，那么我们就不应该继续的去监听，但是，实际我们是需要一直监听对应roster的改变
        //所以，我们把联系人的监听和同步操作放到Service里面去
        unRegisterContentObserver();
        super.onDestroy();
    }

    /*================================监听数据库记录的改变======================================*/
    MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT, true, mMyContentObserver);
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
