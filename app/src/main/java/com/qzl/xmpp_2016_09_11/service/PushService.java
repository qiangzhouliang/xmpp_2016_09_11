package com.qzl.xmpp_2016_09_11.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.qzl.xmpp_2016_09_11.utils.ToastUtils;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by Qzl on 2016-09-14.
 */
public class PushService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("==========PushService onCreate============");
        IMService.conn.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                //转成他的一个子类
                Message message = (Message) packet;
                String body = message.getBody();
//                System.out.println("body = " +body);
                ToastUtils.showToastSafe(getApplicationContext(),body);
            }
        },null);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        System.out.println("==========PushService onDestroy============");
        super.onDestroy();
    }
}
