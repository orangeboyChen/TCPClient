package com.example.Nowcent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.Nowcent.Client.JsonToConnectMessage;
import static com.example.Nowcent.Client.JsonToList;
import static com.example.Nowcent.Client.JsonToUser;
import static com.example.Nowcent.Client.JsonToUserMessage;

public class Main2Activity extends Activity implements View.OnClickListener {
    int port;
    Button btn_Send;
    Button btn_Exit;
    TextView txv_Name;
    TextView txv_Group;
    EditText edt;
    Socket socket;
    Client client;
    Thread recThread;
    Thread sendThread;
    Thread connectThread;
    SimpleAdapter simpleAdapter;
    ListView listView;
    List<Map<String,Object>> msgList=new ArrayList<Map<String,Object>>();
    String[] groupUser;

    CountDownTimer hbTimer=new CountDownTimer(6000,6000) {
        @Override
        public void onTick(long l) {
        }
        @Override
        public void onFinish() {
            disConnect();
            hbTimer.cancel();
        }
    };

    boolean ifAllowThread =true;
    boolean isFront;
    boolean isConnect;
    boolean isExit=false;
    int unreadMsgCount =1;
    User user;

    private static final String TAG = "MainActivity";


    public void reconnect(String msg,boolean isError){
        if(!isError) {
            setList(new ConnectMessage("你",3));
        }
        ifAllowThread=false;
        isConnect=false;
        for(int i=0;i<=3;i++) {
            try {
                SocketAddress socketAddress = new InetSocketAddress(getResources().getString(R.string.ip), 6000);
                socket = new Socket();
                socket.connect(socketAddress, 300);

                client = new Client(socket);
                client.send(new Message(FLAG.RECONNECT,client.UserToJson(new User(user.getName(),user.getPassword(),user.getGroup()))));
                Message message=client.get();
                if(message.getFlag()==FLAG.RECONNECT) {
                    user = JsonToUser(message.getMsg());
                    port=user.getPort();
                    socket = new Socket(getResources().getString(R.string.ip), port);
                    client = new Client(socket);
                    isConnect=true;
                    ifAllowThread=true;
                    new Thread(recThread).start();
                    hbTimer.cancel();
                    hbTimer.start();
                    break;
                }
                Thread.sleep(1000);




//                Log.d(TAG,str);
//                if (str.equals("ALLOW_TO_RECONNECT")) {
//                    client.send("RECONNECT_LICENSE_CHECK|" + userNum+"|"+group);
//                    String preStr = client.get();
//                    Log.d(TAG,preStr);
//                    if (preStr != null) {
//                        String[] strArr = preStr.split("\\|");
//                        if (strArr[0].equals("RECONNECT_SUCCESS")) {
//                            port = Integer.valueOf(strArr[1]);
//                            Log.d(TAG,Integer.toString(port));
//                            socket = new Socket(getResources().getString(R.string.ip), port);
//                            client = new Client(socket);
//                            setTxv_Name(userNickName);
//                            ifConnect=true;
//                            ifAllowThread=true;
//                            new Thread(recThread).start();
//                            hbTimer.cancel();
//                            hbTimer.start();
//                            break;
//                        }
//                    }
//                }
//                Thread.sleep(1000);
            } catch (Exception e) {
                Log.d(TAG,"error socket");
                e.printStackTrace();
            }
        }


        if(!isConnect){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txv_Name.setText(user.getNickName());
                }
            });
            if(!isFront){
                startNotification("未连接","请重新登录",1,true,true);
            }
            Looper.prepare();


            Log.d(TAG,"alert");
            AlertDialog.Builder alert=new AlertDialog.Builder(Main2Activity.this)
            .setTitle("未连接")
            .setMessage("您已退出，请重新登录")
            .setCancelable(false)
            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent=new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(Main2Activity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            alert.show();
            Looper.loop();
        }


    }

    Runnable reconnectRunnable=new Runnable() {
        @Override
        public void run() {
            reconnect("RECONNECT",false);
        }
    };
    Runnable connectRunnable=new Runnable() {
        @Override
        public void run() {
            try{
                socket=new Socket(getResources().getString(R.string.ip),user.getPort());
                client=new Client(socket);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable receiveMsgRunnable=new Runnable() {
        @Override
        public void run() {
            while (ifAllowThread) {
                try{
                    Message message = client.get();
                    if(message!=null) {
                        hbTimer.cancel();
                        hbTimer.start();
                        client.send(new Message(FLAG.HB));
                        handleRecMessage(message);
                    }
                }catch (Exception e){
                    //e.printStackTrace();
                }
            }
        }
    };


    Runnable sendMsgRunnable=new Runnable() {
        @Override
        public void run() {
            try {
                if(!edt.getText().toString().equals("")) {
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("M-d H:mm");
                    Date date=new Date(System.currentTimeMillis());
                    String time=simpleDateFormat.format(date);

                    //Get str
                    String str;
                    do{
                        str = edt.getText().toString();
                    }while(str==null);
                    Log.d(TAG, "str:" + str);

                    UserMessage userMessage=new UserMessage(user.getName(),time,str,user.getGroup());
                    Log.d(TAG,Client.UserMessageToJson(userMessage));
                    client.send(new Message(FLAG.USER_MESSAGE,Client.UserMessageToJson(userMessage)));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edt.setText("");
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void handleRecMessage(Message message){
        UserMessage userMessage;
        ConnectMessage connectMessage;

        if(message!=null)
        switch(message.getFlag()){
            case FLAG.CLOUD:
                userMessage=JsonToUserMessage(message.getMsg());
                setList(userMessage);
                break;
            case FLAG.USER_MESSAGE:
                userMessage=JsonToUserMessage(message.getMsg());
                if(!isFront) {
                    if(unreadMsgCount ==1){
                        startNotification(userMessage.getGroup(), userMessage.getUser()+":"+userMessage.getMsg(),1,true,true);
                    }
                    else{
                        startNotification(userMessage.getGroup(), "["+ unreadMsgCount +"条]"+userMessage.getUser()+":"+userMessage.getMsg(),1,true,true);
                    }
                    unreadMsgCount++;
                }
                else{
                    unreadMsgCount =1;
                }
                setList(userMessage);
                break;
            case FLAG.CONNECT_INFO:
                connectMessage=JsonToConnectMessage(message.getMsg());
                setList(connectMessage);
                break;
            case FLAG.HB:
                client.send(new Message(FLAG.HB));
                break;
            case FLAG.USERLIST:
                //Handle
                String str=message.getMsg();
                String str2=str.replaceAll("[\\[\\]\"]","");
                Log.d(TAG,str2);
                groupUser=str2.split(",");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txv_Group.setText(user.getGroup()+"("+groupUser.length+"人在线）");
                    }
                });
                break;



        }
        }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Unsafe
        //if (android.os.Build.VERSION.SDK_INT > 9) {
            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            //StrictMode.setThreadPolicy(policy);
        //}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent=getIntent();
        isFront=true;
        //Get user
        user=Client.JsonToUser(intent.getStringExtra("user"));
        //Instance
        btn_Exit=(Button)this.findViewById(R.id.btn_exit);
        btn_Send =(Button)this.findViewById(R.id.btn_send);
        txv_Name =(TextView)this.findViewById(R.id.txv_top);
        txv_Group =(TextView)this.findViewById(R.id.txv_group);
        edt=(EditText)this.findViewById(R.id.edt_msg);
        listView=(ListView)this.findViewById(R.id.listview);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txv_Name.setText(user.getNickName());
            }
        });
        txv_Group.setText(user.getGroup());
        recThread=new Thread(receiveMsgRunnable);
        connectThread=new Thread(connectRunnable);

        recThread.start();
        connectThread.start();
        hbTimer.start();

        simpleAdapter=new SimpleAdapter(Main2Activity.this,msgList,R.layout.listview,
                new String[]{"user","time","msg","img","emojiimg"},
                new int[]{R.id.txv_user,R.id.txv_time,R.id.txv_msg,R.id.img_user,R.id.img_emoji});
        listView.setAdapter(simpleAdapter);

        btn_Send.setOnClickListener(this);
        btn_Exit.setOnClickListener(this);
        txv_Group.setOnClickListener(this);


    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_send:

                if(edt.getText().toString().equals("1")){
                    disConnect();
                }
                else if(edt.getText().toString().equals("2")){
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    client.send(new Message(FLAG.USERLIST));
                                }
                            }).start();
                }
                else {
                    sendThread = new Thread(sendMsgRunnable);
                    sendThread.start();
                }
                break;
            case R.id.btn_exit:
                ifAllowThread=false;
                isExit=true;
                new Thread(){
                    public void run(){
                        try {
                            client.send(new Message(FLAG.EXIT));
                            socket.close();
                        }catch (Exception e){}
                    }
                }.start();
                Intent intent=new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(Main2Activity.this,MainActivity.class);
                startActivity(intent);
                simpleAdapter=null;
                listView=null;
                finish();
                break;
            case R.id.txv_group:
                StringBuilder stringBuilder=new StringBuilder();
                for(int i=0;i<groupUser.length-1;i++){
                    stringBuilder.append(groupUser[i]+"\n\r");
                }
                stringBuilder.append(groupUser[groupUser.length-1]+"\n\r");
                String str=stringBuilder.toString();

                AlertDialog.Builder alert=new AlertDialog.Builder(Main2Activity.this)
//                        .setTitle("在线用户")
                        .setMessage(str)
                        .setCancelable(true)
                        .setPositiveButton("好", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                alert.show();
                break;
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        isFront=false;
        //startNotification("即讯正在后台运行","请不要清除进程",2,false,false);
        Log.d(TAG,"STOP");
    }

    @Override
    public void onStart(){
        super.onStart();
        isFront=true;
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        unreadMsgCount =1;
        Log.d(TAG,"START");
    }

    private void disConnect(){
        if(!isExit) {
            Log.d(TAG, "disConnect()");
            new Thread(reconnectRunnable).start();
        }
    }

    public void startNotification(String title,String msg,int id,boolean cancelable,boolean importance){
        Log.d(TAG,"Notification");
        Intent intent=new Intent(getApplicationContext(),Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent=PendingIntent.getActivity(Main2Activity.this,10,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            Log.d(TAG,"Notification8888888888888888888888");
            NotificationChannel notificationChannel = new NotificationChannel("1", "消息提醒", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification.Builder builder=new Notification.Builder(this)
            .setContentTitle(title)
            .setContentText(msg)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setSmallIcon(R.mipmap.iclauncher)
            .setContentIntent(pendingIntent)
            .setNumber(unreadMsgCount)
            .setAutoCancel(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setChannelId("1");
            notification=builder.build();
            //if(!cancelable){
            //    notification.flags|=Notification.FLAG_ONGOING_EVENT;
            //}
            notificationManager.notify(id,notification);
            return;
        }
        else{
            NotificationCompat.Builder builder=new NotificationCompat.Builder(Main2Activity.this.getApplicationContext())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setDefaults(Notification.DEFAULT_ALL)
            .setTicker(title+"："+msg)
            .setContentTitle(title)
            .setContentText(msg)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent);
            notification=builder.build();
            if(!cancelable){
                notification.flags|=Notification.FLAG_ONGOING_EVENT;
            }
            notificationManager.notify(1,notification);

        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(false);
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void setList(UserMessage userMessage){
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("user",userMessage.getUser());
        map.put("time",userMessage.getTime());
        map.put("msg",userMessage.getMsg());
        if (userMessage.getUser().equals("GPM")) {
            map.put("img", R.drawable.gpm_png);
        } else if (userMessage.getUser().equals("orangeboy")) {
            map.put("img", R.drawable.admin_png);
        } else {
            map.put("img", R.drawable.user_png);
        }
        msgList.add(map);
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        simpleAdapter.notifyDataSetChanged();
                    }
                }
        );

    }

    private void setList(ConnectMessage connectMessage){
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("user","系统");
        switch(connectMessage.getType()){
            case 1:
                map.put("time", connectMessage.getName()+"已加入");
                break;
            case 2:
                map.put("time", connectMessage.getName()+"已退出");
                break;
            case 3:
                map.put("time", connectMessage.getName()+"正在重连");
                break;
        }
        msgList.add(map);
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        simpleAdapter.notifyDataSetChanged();
                    }
                }
        );

    }


}
