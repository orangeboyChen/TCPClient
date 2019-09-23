package com.orangeboy.Nowcent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.orangeboy.Nowcent.Client.JsonToConnectMessage;
import static com.orangeboy.Nowcent.Client.JsonToListStrs;
import static com.orangeboy.Nowcent.Client.JsonToListUserMessage;
import static com.orangeboy.Nowcent.Client.JsonToUser;
import static com.orangeboy.Nowcent.Client.JsonToUserMessage;
import static com.orangeboy.Nowcent.Client.UserMessageToJson;

public class ChatActivity extends Activity implements View.OnClickListener {
    private int port;
    private int latestMessageId=0;
    private String group;
    private Button btn_Send;
    private Button btn_Exit;
    private TextView txv_Name;
    private TextView txv_Group;
    private EditText edt;
    private Socket socket;
    private Client client;
    private Thread recThread;
    private Thread sendThread;
    private Thread connectThread;
    private ListView listView;
    private List<String> groupUser;
    private Adapter adapter;
    private ArrayList<ListItem> arrayList;
    private SQLiteDatabase sqLiteDatabase;
    private ProgressDialog progressDialog;
    private final int LOCAL=1;
    private final int CLOUD=2;

    CountDownTimer hbTimer=new CountDownTimer(6000,6000) {
        @Override
        public void onTick(long l) {
        }
        @Override
        public void onFinish() {
            reconnect();
            hbTimer.cancel();
        }
    };

    boolean isAllowThread =true;
    boolean isFront=true;
    boolean isConnect=true;
    boolean isExit=false;
    int unreadMsgCount =1;
    int defaultValue=0;
    User user;

    private static final String TAG = "LoginActivity";


    public void reconnect(String msg,boolean isError){
        groupUser=null;


//        if(!isError) {
//            setList(new Message_Connect("你",3));
//        }
        if(isFront){
            setList(new Message_Connect("你",3));
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txv_Group.setText(user.getGroup()+"(你当前离线)");
            }
        });
        isAllowThread =false;
        isConnect=false;
        for(int i=0;i<=2;i++) {
            try {
                //Connect
                SocketAddress socketAddress = new InetSocketAddress(getResources().getString(R.string.ip), 6000);
                socket = new Socket();
                socket.connect(socketAddress, 300);

                //Connect successfully
                client = new Client(socket);

                //Send RECONNECT message
                client.send(new Message(FLAG.RECONNECT,Client.UserToJson(new User(user.getName(),user.getPassword(),group))));

                //Get message from server
                Message message=client.get();

                //Analyse the message
                if(message.getFlag()==FLAG.RECONNECT) {
                    user = JsonToUser(message.getMsg());//Set user
                    port=user.getPort();//Set port
                    socket = new Socket(getResources().getString(R.string.ip), port);//Connect private server
                    client = new Client(socket);



                    //Initialize variables
                    isConnect=true;
                    isAllowThread =true;

                    Message message2=client.get();
                    handleRecMessage(message2);


                    //Get cloud message
                    Message message1=null;
                    List<UserMessage> userMessages=null;
                    do{
                        try {
                            client.send(new Message(FLAG.CLOUD_REQUEST, Client.MessageCloudToJson(new Message_Cloud(latestMessageId))));
                            message1 = client.get();
                            if(message1.getFlag()==FLAG.CLOUD){
                                userMessages = JsonToListUserMessage(message1.getMsg());
                                if(userMessages.size()!=0){
                                    Database.UserMessagesDatabase.addUserMessagesListToDatabase(userMessages);
                                    latestMessageId=userMessages.get(userMessages.size()-1).getMessageId();
                                }
                                break;
                            }
                        }catch(Exception e){e.printStackTrace();}
                    }while (true);
                    arrayList.remove(arrayList.size()-1);
                    setList(userMessages,CLOUD);

                    //Restart recThread
                    new Thread(recThread).start();

                    //Restart HB
                    hbTimer.cancel();
                    hbTimer.start();

                    break;
                }
                Thread.sleep(1000);
            }
            catch (Exception e) {
                Log.d(TAG,"error socket");
                e.printStackTrace();
            }
        }

        if(!isConnect){
            if(isFront){
                Looper.prepare();
                //Show Dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this)
                        .setTitle("未连接")
                        .setMessage("您已退出，请重新登录")
                        .setCancelable(false)
                        .setPositiveButton("好", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setClass(ChatActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                alert.show();
                Looper.loop();
            }
            else{
                startNotification("未连接", "请重新登录", 1, true, true);
            }
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
                Message message2=client.get();
                handleRecMessage(message2);


                //Get cloud message
                Message message1=null;
                List<UserMessage> userMessages=null;
                do{
                    try {
                        client.send(new Message(FLAG.CLOUD_REQUEST, Client.MessageCloudToJson(new Message_Cloud(latestMessageId))));
                        message1 = client.get();
                        if(message1.getFlag()==FLAG.CLOUD){
                            userMessages = JsonToListUserMessage(message1.getMsg());
                            if(userMessages.size()!=0){
                                Database.UserMessagesDatabase.addUserMessagesListToDatabase(userMessages);
                                latestMessageId=userMessages.get(userMessages.size()-1).getMessageId();
                            }
                            break;
                        }
                    }catch(Exception e){e.printStackTrace();}
                }while (true);

                recThread.start();
                setList(userMessages,CLOUD);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable receiveMsgRunnable=new Runnable() {
        @Override
        public void run() {
            while (isAllowThread) {
                try{
                    Message message = client.get();
                    if(message!=null) {
                        hbTimer.cancel();
                        hbTimer.start();
                        client.send(new Message(FLAG.HB));
                        handleRecMessage(message);
                    }
                }
                catch (Exception e){
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
//                    Log.d(TAG, "str:" + str);

                    UserMessage userMessage=new UserMessage(user.getName(),str,user.getGroup(),UserMessage.MSG);
//                    Log.d(TAG,Client.UserMessageToJson(userMessage));
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
        Message_Connect messageConnect;

        if(message!=null)
        switch(message.getFlag()){
//            case FLAG.CLOUD:
//                List<UserMessage> userMessages=JsonToListUserMessage(message.getMsg());
//                latestMessageId=userMessages.get(userMessages.size()-1).getMessageId();
//                Database.UserMessagesDatabase.addUserMessagesListToDatabase(userMessages);
//                setList(userMessages);
//                break;
            case FLAG.USER_MESSAGE:
                userMessage=JsonToUserMessage(message.getMsg());
                latestMessageId=userMessage.getMessageId();
                Database.UserMessagesDatabase.addUserMessageToDatabase(userMessage);
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
                messageConnect =JsonToConnectMessage(message.getMsg());
                if(!messageConnect.getName().equals(user.getName())) {
                    setList(messageConnect);
                }
                break;
            case FLAG.HB:
                client.send(new Message(FLAG.HB));
                break;
            case FLAG.USERLIST:
                //Handle
//                String str=message.getMsg();
//                String str2=str.replaceAll("[\\[\\]\"]","");
//                Log.d(TAG,str2);
//                groupUser=str2.split(",");
                groupUser=JsonToListStrs(message.getMsg());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txv_Group.setText(user.getGroup()+"("+groupUser.size()+")");
                    }
                });
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get data from loginActivity
        setContentView(R.layout.activity_chat);
        Intent intent=getIntent();
        isFront=true;

        //Get user
        user=Client.JsonToUser(intent.getStringExtra("user"));
        group=user.getGroup();

        //Instance
        btn_Exit=(Button)this.findViewById(R.id.btn_exit);
        btn_Send =(Button)this.findViewById(R.id.btn_send);
        txv_Name =(TextView)this.findViewById(R.id.txv_top);
        txv_Group =(TextView)this.findViewById(R.id.txv_group);
        edt=(EditText)this.findViewById(R.id.edt_msg);
        listView=(ListView)this.findViewById(R.id.listview);
        recThread=new Thread(receiveMsgRunnable);
        connectThread=new Thread(connectRunnable);

        //Set UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txv_Name.setText(user.getNickName());
            }
        });
        txv_Group.setText(user.getGroup());

        //Set Listview adapter
        arrayList=new ArrayList<>();
        adapter=new Adapter(ChatActivity.this,arrayList);
        listView.setAdapter(adapter);

        sqLiteDatabase=Database.getSqLiteDatabase(ChatActivity.this);
        List<UserMessage> list=Database.UserMessagesDatabase.getUserMessagesList();
        if(list.size()!=0){
            latestMessageId=list.get(list.size()-1).getMessageId();
        }
        try {
            Log.d(TAG, list.get(0).getMsg());
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"error database");
        }
        setList(list,LOCAL);

        connectThread.start();
        hbTimer.start();

        //Set OnClickListener and TextWatcher
        btn_Send.setOnClickListener(this);
        btn_Exit.setOnClickListener(this);
        txv_Group.setOnClickListener(this);
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(edt.getText().toString().equals("/")){
                    final String[] emojis={"GPM","Happy"};

                    AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this)
                            .setTitle("发送表情")
                            .setItems(emojis,new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    defaultValue=i;
                                    Log.d(TAG,Integer.toString(i));
                                    switch (defaultValue){
                                        case 0:
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    client.send(new Message(FLAG.USER_MESSAGE,UserMessageToJson(new UserMessage(user.getName(),"[GPM的动画表情]",R.drawable.gpm,user.getGroup(),UserMessage.IMG))));
                                                }
                                            }).start();
                                            break;
                                        case 1:
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    client.send(new Message(FLAG.USER_MESSAGE,UserMessageToJson(new UserMessage(user.getName(),"[动画表情]",R.drawable.happy,user.getGroup(),UserMessage.IMG))));
                                                }
                                            }).start();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            edt.setText("");
                                        }
                                    });
                                }
                            });
                    builder.show();
                    Looper.loop();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_send:
//
//                if(edt.getText().toString().equals("1")){
//                    reconnect();
//                }
//                else if(edt.getText().toString().equals("2")){
//                    new Thread(
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    client.send(new Message(FLAG.USERLIST));
//                                }
//                            }).start();
//                }
//                else {
                    sendThread = new Thread(sendMsgRunnable);
                    sendThread.start();
//                }
                break;
            case R.id.btn_exit:
                isAllowThread =false;
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
                intent.setClass(ChatActivity.this, LoginActivity.class);
                startActivity(intent);
                adapter=null;
                listView=null;
                finish();
                break;
            case R.id.txv_group:
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < groupUser.size() - 1; i++) {
                        stringBuilder.append(groupUser.get(i) + "\n");
                    }
                    stringBuilder.append(groupUser.get(groupUser.size() - 1));
                    String str = stringBuilder.toString();
                    AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this)
//                        .setTitle("在线用户")
                            .setMessage(str)
                            .setCancelable(true)
                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    alert.show();
                }catch (Exception e){
                    AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this)
                            .setTitle("不要着急哦")
                            .setCancelable(true)
                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                }
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
        if(!isConnect){
            reconnect();
        }

        Log.d(TAG,"START");
    }

    private void reconnect(){
        if(!isExit) {
            Log.d(TAG, "reconnect()");
            new Thread(reconnectRunnable).start();
        }
    }

    public void startNotification(String title,String msg,int id,boolean cancelable,boolean importance){
        Log.d(TAG,"Notification");
        Intent intent=new Intent(getApplicationContext(), ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent=PendingIntent.getActivity(ChatActivity.this,10,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
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
            NotificationCompat.Builder builder=new NotificationCompat.Builder(ChatActivity.this.getApplicationContext())
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
        arrayList.add(new ListItem(userMessage));
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                }
        );

    }

    private void setList(Message_Connect messageConnect){
        arrayList.add(new ListItem(messageConnect));
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }

    private void setList(final List<UserMessage> list,int flag){
        Iterator iterator=list.iterator();
        while(iterator.hasNext()){
            arrayList.add(new ListItem((UserMessage) iterator.next()));
        }

        if(list.size()!=0) {
            switch (flag) {
                case LOCAL:
                    arrayList.add(new ListItem("以上为本地信息"));
                    break;
                case CLOUD:
                    arrayList.add(new ListItem("以上为最近信息"));
                    break;
            }
        }
//        else{
//            arrayList.add(new ListItem("未获取到信息"));
//        }
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        listView.setSelectionFromTop(listView.getCount()-1,0);

                    }
                }
        );
    }

    public ChatActivity getContext(){
        return ChatActivity.this;
    }

//    public void setList(UserImg userImg){
//        arrayList.add(new ListItem(userImg));
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                adapter.notifyDataSetChanged();
//            }
//        });
//    }


    private void showProgressDialog(String str){
        try {
            Looper.prepare();
        }catch (Exception e){}
        progressDialog=new ProgressDialog(ChatActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(str);
        progressDialog.show();
        Looper.loop();
    }
}
