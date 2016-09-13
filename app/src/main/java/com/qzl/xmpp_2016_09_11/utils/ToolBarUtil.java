package com.qzl.xmpp_2016_09_11.utils;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qzl.xmpp_2016_09_11.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qzl on 2016-09-12.
 */
public class ToolBarUtil {

    private List<TextView> mTextViews = new ArrayList<>();//用来保存TextView

    public void createToolBar(LinearLayout container, String[] toolBarTitleArr, int[] iconArr){

        for (int i = 0; i < toolBarTitleArr.length; i++) {
            TextView tv = (TextView) View.inflate(container.getContext(), R.layout.inflate_toolbar_btn,null);
            tv.setText(toolBarTitleArr[i]);
            //动态修改drawableTop属性
            tv.setCompoundDrawablesWithIntrinsicBounds(0,iconArr[i],0,0);

            int width = 0;
            int height = ActionBar.LayoutParams.MATCH_PARENT;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
            //设置weight属性
            params.weight = 1;
            container.addView(tv,params);

            //保存TextView到集合中
            mTextViews.add(tv);

            //社置点击事件
            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //模块之间传值，需要接口回调
                    // 3 需要传值的地方用接口对象，调运接口方法
                    mOnToolBarClickListener.onToolBarClick(finalI);
                }
            });
        }
    }

    public void changeColor(int position){
        //还原所有的颜色
        for (TextView tv : mTextViews) {
            tv.setSelected(false);
        }
        mTextViews.get(position).setSelected(true);//通过设置selected属性设置为选中效果
    }

    // 1 创建接口和接口方法
    public interface OnToolBarClickListener{
        void onToolBarClick(int position);
    }
    //2 定义接口变量
    OnToolBarClickListener mOnToolBarClickListener;

    // 4 暴露一个公共方法
    public void setOnToolBarClickListener(OnToolBarClickListener onToolBarClickListener) {
        mOnToolBarClickListener = onToolBarClickListener;
    }
}
