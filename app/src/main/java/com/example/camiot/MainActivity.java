package com.example.camiot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private WebView cctvweb; //웹뷰
    private WebSettings webSettings; //웹뷰 세팅

    private Button btnOn; //cctv 켜기 버튼
    private Button btnOff; //cctv 끄기 버튼
    private Button btnReport; //신고하기 버튼
    private Button btnAlarmOn; //알림 켜기 버튼
    private Button btnAlarmOff; //알림 끄기 버튼

    //알람이 On/Off인지를 저장하는 변수
    public static  boolean isAlram = true;
    //움직임 감지 Count
    public static  int count = 0;
    //MqttClient
    private MqttClient mqttClient;

    //학교 서버(라즈베리 파이) : 192.168.15.84
    private String camUrl = "http://192.168.15.84:8081";
    //mqtt server url(라즈베리파이)
    private String mqttUrl = "tcp://192.168.15.84:1883";

//    private String camUrl = "http://192.168.10.117:8081";
//    private String mqttUrl = "tcp://192.168.10.117:1883";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cctvweb = (WebView)findViewById(R.id.cctvweb);
        btnOn = (Button)findViewById(R.id.btnOn);
        btnOff = (Button)findViewById(R.id.btnOff);
        btnReport = (Button)findViewById(R.id.btnReport);
        btnAlarmOn = (Button)findViewById(R.id.btnAlarmOn);
        btnAlarmOff = (Button)findViewById(R.id.btnAlarmOff);


        webSettings = cctvweb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        cctvweb.loadData("<img style = '-webkit-user-select: none;margin: auto;background-color: hsl(0, 0%, 25%);' src = 'http://192.168.15.84:8081/' width='640' height='480'>",

                "text/html", "UTF-8");



        try {
            mqttClient = new MqttClient(mqttUrl,MqttClient.generateClientId(),null);
            mqttClient.connect();
            mqttClient.subscribe("detection");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.i("MQTT connect fail", "다시 연결 필요!!");
        }

        //mqttclient message
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("connectionLost","Mqtt ReConnect");
            }

            //mqtt message가 도착했을 때
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                //알람이 On일때
                if(isAlram){
                    //움직임 감지가 연속으로 3번된다면 알림
                    Log.i("토픽 : ", topic +"\t메세지 : " +  message.toString());
                    if(count == 0){
                        Log.i("count ==> ", count+"");
                        count++;
                    }else if(count == 1){
                        Log.i("count ==> ", count+"");
                        count++;
                    }else if(count == 2){
                        Log.i("count ==> ", count+"");
                        //알림생성
                        createNotification();
                        Log.i("detection =>","움직임 감지!!");
                        count = 0;
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


        //CCTV 켜기 버튼
        btnOn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i("btnOn", "btnOnClick");
                isAlram = true;
                cctvweb.setVisibility(View.VISIBLE);
            }
        });

        //CCTV 끄기 버튼
        btnOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i("btnOff", "btnOffClick");
                isAlram = false;
                cctvweb.setVisibility(View.INVISIBLE);
            }
        });

        //신고하기 버튼
        btnReport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("신고");
                builder.setMessage("신고하시겠습니까?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //알림 켜기 버튼
        btnAlarmOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAlram = true;
                Toast.makeText(getApplicationContext(),"알람이 켜졌습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //알림 켜기 버튼
        btnAlarmOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAlram = false;
                Toast.makeText(getApplicationContext(),"알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //알림 생성 함수
    private void createNotification(){

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.motionicon);
        builder.setContentTitle("움직임 감지!!");
        builder.setContentText("움직임이 감지되었습니다. 확인 후 신고하세요!!");
        builder.setContentIntent(notificationPendingIntent);

        builder.setColor(Color.RED);
        //사용자가 탭을 누르면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());

    }

    //알림 제거
    private void removeNotification() {
        NotificationManagerCompat.from(this).cancel(1);
    }
}