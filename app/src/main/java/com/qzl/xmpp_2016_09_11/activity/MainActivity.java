package com.qzl.xmpp_2016_09_11.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qzl.xmpp_2016_09_11.R;
import com.qzl.xmpp_2016_09_11.fragment.ContactsFragment;
import com.qzl.xmpp_2016_09_11.fragment.SessionFragment;
import com.qzl.xmpp_2016_09_11.utils.ToolBarUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {


    @InjectView(R.id.activity_main_tv_title)
    TextView mTvTitle;

    @InjectView(R.id.activity_main_vp_viewpager)
    ViewPager mViewpager;

    @InjectView(R.id.main_bottom)
    LinearLayout mBottom;

    private List<Fragment> mFragments = new ArrayList<>();
    private ToolBarUtil mToolBarUtil;
    private String[] mToolBarTitleArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        initDate();
        initListener();
    }

    /**
     * 监听事件
     */
    private void initListener() {
        mViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            //选中的时候
            @Override
            public void onPageSelected(int position) {
                //修改底部toolBar颜色
                mToolBarUtil.changeColor(position);
                //修改title
                mTvTitle.setText(mToolBarTitleArr[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mToolBarUtil.setOnToolBarClickListener(new ToolBarUtil.OnToolBarClickListener() {
            @Override
            public void onToolBarClick(int position) {
                mViewpager.setCurrentItem(position);
            }
        });
    }

    private void initDate() {
        //viewPager-->view-->pagerAdapter
        //viewPager-->fragment-->fragemntpagerAdapter-->fragment数量少
        //viewPager-->fragment-->fragmentStatePagerAdapter

        //添加fragment到集合中
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactsFragment());
        mViewpager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        //底部按钮
        mToolBarUtil = new ToolBarUtil();
        //文字内容
        mToolBarTitleArr = new String[]{"会话","联系人"};
        //图标内容
        int[] iconArr = {R.drawable.selecter_meassage_selector,R.drawable.selecter_selfinfo_selector};
        mToolBarUtil.createToolBar(mBottom, mToolBarTitleArr,iconArr);

        //设置默认选中会话
        mToolBarUtil.changeColor(0);
    }

    class MyPagerAdapter extends FragmentPagerAdapter{

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
