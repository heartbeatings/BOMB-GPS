package com.wjj.demo;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;
import android.os.Vibrator;
import android.telephony.SmsManager;
import com.apkfuns.logutils.LogUtils;
import com.wjj.demo.BroadcastReceiver.SMSBroadcastReceiver;
import com.wjj.demo.Model.PhoneMessage;
import java.util.StringTokenizer;

import java.util.List;



import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;


import butterknife.BindView;
import butterknife.ButterKnife;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class MainActivity extends BaseActivity {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private SMSBroadcastReceiver mSMSBroadcastReceiver;
    private List<String> intentList;
    private Handler handler;
    private String cmd = "";
    private String Num = "";
    private String L1 = "";
    private String L2 = "";

    private IntentFilter sendFilter;

    @BindView(R.id.text)
    TextView text;

    String Code = "1111";

    @Override
    int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    void init() {


        ButterKnife.bind(this);
        intentList = getIntent().getStringArrayListExtra("dataList");
    }

    int is = 0;

    @Override
    void logic() {
        //授权
        PermissionGen.with(MainActivity.this)
                .addRequestCode(100)
                .permissions(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS)
                .request();

        //生成广播处理

        mSMSBroadcastReceiver = new SMSBroadcastReceiver();
        mSMSBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {
            @Override
            public void onReceived(PhoneMessage phoneMessage) {
                Num = intentList.get(0);

                for (int k = 0; k < intentList.size(); k++) {

                    if (phoneMessage.getPhoneNumber().equals(intentList.get(k))) {
                        is = 1;


                        String msg = phoneMessage.getPhoneNumber() + "\n" + phoneMessage.getMsgTime() + "\n" + phoneMessage.getMsgContent();
                        LogUtils.d("--->发送的对象是 " + msg);
                        text.setText(msg);
                        StringTokenizer token = new StringTokenizer(phoneMessage.getMsgContent(), "#");
                        String[] array = new String[3];//定义一个字符串数组
                        int i = 0;
                        while (token.hasMoreTokens()) {
                            array[i] = token.nextToken();//将分割开的子字符串放入数组中
                            i++;
                        }

                        if (array[0].equals("BOMB") && array[1].equals(Code)) {
                            cmd = "引爆成功";
                            // smsMgr.sendTextMessage(phoneMessage.getPhoneNumber(), null, "引爆成功!", null, null);
                          send();
                            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            long[] pattern = {800, 50, 400, 30};
                            vibrator.vibrate(pattern, 0);
                        } else if (array[0].equals("CHANGE") &&array[1].equals(Code)) {
                            Code = array[2];
                            cmd = "修改成功";
                          send();
                            //smsMgr.sendTextMessage(phoneMessage.getPhoneNumber(), null, "修改成功!", null, null);

                        }
                        else if(array[0].equals("GPS") && array[1].equals(Code)){
                            calculate();
                            cmd = "经度为：113°" + L1 + "\'\n" + "纬度为：28°" + L2 + "\'";
                            //      smsMgr.sendTextMessage(phoneMessage.getPhoneNumber(), null, "经度为：28°10'"+"\n"+"纬度为：28°10'" , null, null);
                            send();
                        }
                        else if(array[1].equals(Code)){
                            cmd = "指令错误!";
                            send();
                            //smsMgr.sendTextMessage(phoneMessage.getPhoneNumber(), null, "指令错误!", null, null);
                        }
                        else{
                            cmd = "密码错误!";
                          send();
                            //smsMgr.sendTextMessage(phoneMessage.getPhoneNumber(), null, "密码错误!", null, null);
                        }


                    }
                }
                if (is == 0) {
                    cmd = "未授权者请求："+'\n'+phoneMessage.getPhoneNumber() + '\n' + phoneMessage.getMsgTime() + '\n' + phoneMessage.getMsgContent();
                    //smsMgr.sendTextMessage(intentList.get(0), null, "未授权者请求："+'\n'+phoneMessage.getPhoneNumber() + '\n' + phoneMessage.getMsgTime() + '\n' + phoneMessage.getMsgContent(), null, null);
                    send();
                }
                text.setText(cmd);
            }
        });

    }
   public void send(){
        String temp = cmd;
        sendFilter = new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        final SmsManager smsManager = SmsManager.getDefault();
        Intent sentIntent = new Intent("SENT_SMS_ACTION");
        final PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, sentIntent, 0);
        smsManager.sendTextMessage(Num, null, temp, pi, null);
    }

    @Override
    void onResumeInit() {


    }

    @Override
    void onResumeLogic() {

    }


    @PermissionSuccess(requestCode = 100)
    public void doRegisterReceiver() {
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter(ACTION);
        intentFilter.setPriority(1000);
        //注册广播
        this.registerReceiver(mSMSBroadcastReceiver, intentFilter);
    }

    @PermissionFail(requestCode = 100)
    public void doFailRegisterReceiver() {
        Toast.makeText(this, "Contact permission is not granted", Toast.LENGTH_SHORT).show();
    }

    public void calculate(){
        int max=8;
        int min=2;
        Random random = new Random();

        int s1 = random.nextInt(max)%(max-min+1) + min + 20;
        int s2 = random.nextInt(max)%(max-min+1) + min;

        L1 = Integer.toString(s1);
        L2 = Integer.toString(s2);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }




}


