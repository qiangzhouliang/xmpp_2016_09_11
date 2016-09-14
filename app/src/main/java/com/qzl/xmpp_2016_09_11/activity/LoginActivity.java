package com.qzl.xmpp_2016_09_11.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qzl.xmpp_2016_09_11.R;
import com.qzl.xmpp_2016_09_11.service.IMService;
import com.qzl.xmpp_2016_09_11.service.PushService;
import com.qzl.xmpp_2016_09_11.utils.ThreadUtils;
import com.qzl.xmpp_2016_09_11.utils.ToastUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class LoginActivity extends AppCompatActivity {
    private static final String HOST = "10.4.10.12";//主机ip
    private static final int PORT = 5222;//对应端口号
    public static final String SERVICENAME = "qzl.com";

    private EditText mEtpassword;
    private EditText mEtUserName;
    private Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        initListener();
    }

    /**
     * 点击事件
     */
    private void initListener() {
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mEtUserName.getText().toString();
                final String password = mEtpassword.getText().toString();
                //判断用户名是否为空
                if (TextUtils.isEmpty(username)){
                    //用户名为空
                    mEtUserName.setError("用户名不能为空");
                    return;
                }
                // 判断密码是否为空
                if (TextUtils.isEmpty(password)){
                    mEtpassword.setError("密码不能为空");
                }
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //创建链接配置对象
                            ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
                            //额外配置（方便我们开发）
                            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文传输
                            config.setDebuggerEnabled(true);//开启调试模式，方便我们查看具体发送的内容
                            //开始创建链接对象
                            XMPPConnection conn = new XMPPConnection(config);
                            //开始连接
                            conn.connect();
                            //连接成功了
                            //开始登录
                            conn.login(username,password);
                            //已经登录成功
                            ToastUtils.showToastSafe(getApplicationContext(),"登录成功");
                            //跳到主界面
                            finish();
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);

                            //需要保存连接对象
                            IMService.conn = conn;
                            //保存当前登录的账户
                            String account = username+"@"+LoginActivity.SERVICENAME;
                            IMService.mCurAccount = account;//admin-->admin@qzl.com

                            //需要启动services
                            startService(new Intent(LoginActivity.this,IMService.class));
                            //启动推送的服务
                            startService(new Intent(LoginActivity.this, PushService.class));
                        } catch (final XMPPException e) {
                            e.printStackTrace();
                            ToastUtils.showToastSafe(getApplicationContext(),"登录失败");
                        }
                    }
                });
            }
        });
    }

    private void initView() {
        mEtUserName = (EditText) findViewById(R.id.activity_login_et_username);
        mEtpassword = (EditText) findViewById(R.id.activity_login_et_password);
        mBtnLogin = (Button) findViewById(R.id.activity_login_btn_login);
    }
}
