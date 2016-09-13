package com.qzl.xmpp_2016_09_11.utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * 汉字转拼音
 * Created by Qzl on 2016-09-12.
 */
public class PinyinUtil {
    public static String getPinyin(String str){
        String pinyinString = PinyinHelper.convertToPinyinString(str, "", PinyinFormat.WITHOUT_TONE);
        return pinyinString;
    }
}
