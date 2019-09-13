package com.example.Nowcent;

import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.google.gson.Gson;

public class MainActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    //Variables
    private int versionCode;
    private boolean isAllowThread = true;
    private boolean isAllowToUse=true;
    private boolean isFirstTime = true;
    private static final String TAG = "MainActivity";//Log
    //Object
    private Button btn_Login;
    private EditText edt_UserName;
    private EditText edt_Password;
    private EditText edt_Group;
    private Socket socket;
    private Client client;
    private Message message;
    private User user;

    Runnable checkIfConnected = new Runnable() {
        @Override
        public void run() {
            if (!isAllowToUse) {
                new Thread(checkVersion).start();
            } else {
                try {
//                    setBtnEnabled(false);//set button
                    Looper.prepare();

                    //set socket
                    SocketAddress socketAddress = new InetSocketAddress(getResources().getString(R.string.ip), 6000);
                    socket = new Socket();
                    socket.connect(socketAddress, 300);
                    client = new Client(socket);
                    client.send(new Message(FLAG.LOGIN));
                    while (isAllowThread) {
                        Message loginMessage = client.get();
                        //Log.d(TAG, "str:" + str);
                        if (loginMessage.getFlag()==FLAG.ALLOW) {
                            break;
                        } else if (loginMessage.getFlag()==FLAG.REFUSE) {
                            showAlert("你已被禁止登录", "请联系管理员");
                            Looper.loop();
                            //socket.close();
                            break;
                        }
                    }
                    setBtnEnabled(true);
                    //Log.d(TAG,"33333333333333333333333");
                    client.send(new Message(FLAG.CHECK_USER,Client.UserToJson(new User(edt_UserName.getText().toString(),edt_Password.getText().toString(),edt_Group.getText().toString()))));
                    Message checkMessage = client.get();
                    //Log.d(TAG, "recstr:" + recStr);
                    /*
                    if (recStr != null) {
                        if (handleRecMsg(recStr.split("\\|"))) {
                            Intent intent = new Intent()
                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            .setClass(MainActivity.this, Main2Activity.class)
                            .putExtra("userNickName", validUser)
                            .putExtra("port", Integer.toString(insidePort))
                            .putExtra("userNum",Integer.toString(userNum))
                            .putExtra("group",group);
                            isAllowThread = false;
                            socket.close();
                            startActivity(intent);
                        } else {
                            socket.close();
                        }
                    }
                    */
                    if(checkMessage.getFlag()==FLAG.CHECK_USER){
                        user=Client.JsonToUser(checkMessage.getMsg());
                        //set Sp
                        setSp("userName",user.getName());
                        setSp("password",user.getPassword());
                        setSp("group",user.getGroup());

                        //Prepare activity
                        Intent intent = new Intent()
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                .setClass(MainActivity.this, Main2Activity.class)
                                .putExtra("user",Client.UserToJson(user));
                        isAllowThread = false;
                        socket.close();
                        startActivity(intent);
                    }
                    else if(checkMessage.getFlag()==FLAG.LOGIN_ERROR){
                        showAlert("密码错误","请重新输入用户名和密码");
                        //Set Sp
                        setSp("password",null);
                        setSp("group",null);
                        //Clear
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                edt_Password.setText("");
                                edt_Group.setText("");
                            }
                        });
                    }
                    else if(checkMessage.getFlag()==FLAG.LOGIN_USING){
                        showAlert("有人正在登录你的账号","如果这的确是你的账号，请稍后再登录");
                        setBtnEnabled(true);
                        Looper.loop();
                    }
                    else {
                        showAlert("连接失败", "服务器返回了错误的数据");
                        setBtnEnabled(true);
                        Looper.loop();
                    }
                } catch (java.net.SocketTimeoutException e) {
                    showAlert("连接失败", "请稍后再试");
                    setBtnEnabled(true);
                    Looper.loop();
                } catch (java.net.ConnectException e) {
                    showAlert("连接失败", "请检查你的网络连接");
                    setBtnEnabled(true);
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    Runnable checkVersion = new Runnable() {
        @Override
        public void run() {
            while (isAllowThread) {
                /*
                try {
                    //new socket to connect port 6002
                    Socket socket = new Socket(getResources().getString(R.string.ip), 6002);
                    Client client = new Client(socket);

                    client.send(new Message(1,null));
                    Message message = client.get();
                    //Version
                    final String[] strArr;
                    if (message.getFlag()==2) {
                        //strArr = versionStr.split("\\|");
                        if (strArr[1].equals("VERSION_BACK")) {
                            if (getLocalVersionCode()==Integer.valueOf(strArr[2])) {
                                break;
                            } else {
                                if (getLocalVersionCode()>=Integer.valueOf(strArr[3])) {
                                    Looper.prepare();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("发现了新的版本" + strArr[4])
                                            .setMessage("更新日志：\r\n" + strArr[5])
                                            .setCancelable(false)
                                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            })
                                            .setNegativeButton("下载该版本", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Uri uri = Uri.parse(strArr[6]);
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(intent);
                                                }
                                            })
                                            .create().show();
                                    Looper.loop();
                                    break;
                                } else {
                                    isAllowToUse=false;
                                    Looper.prepare();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("发现了新的版本" + strArr[4])
                                            .setMessage("您当前的版本过旧，若拒绝更新将无法继续使用！\r\n更新日志：\r\n" + strArr[5])
                                            .setCancelable(false)
                                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Uri uri = Uri.parse(strArr[6]);
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(intent);
                                                }
                                            })
                                            .create().show();
                                    Looper.loop();
                                    break;
                                }
                            }
                        }
                    }
                    */
                try{
                    Socket socket = new Socket(getResources().getString(R.string.ip), 6002);
                    Client client = new Client(socket);

                    String updateLog;
                    final String updateURL;
                    int newVersionCode;
                    int minVersionCode;
                    String versionName;
                    int localVersionCode= getLocalVersionCode();


                    client.send(new Message(FLAG.VERSION));
                    message=client.get();
                    if(message.getFlag()==FLAG.VERSION){
                        String[] strings=message.getMsg().split("\\|");
                        updateLog = strings[0];
                        updateURL = strings[1];
                        newVersionCode = Integer.valueOf(strings[2]);
                        minVersionCode = Integer.valueOf(strings[3]);
                        versionName = strings[4];
                    }
                    else{
                        continue;
                    }

                    if(localVersionCode>=newVersionCode){
                        break;
                    }
                    else{
                        if(localVersionCode>=minVersionCode){
                            Looper.prepare();
                            //Show dialog
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("发现了新的版本" + versionName)
                                    .setMessage("更新日志：\r\n" + updateLog)
                                    .setCancelable(false)
                                    .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    })
                                    .setNegativeButton("下载该版本", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Uri uri = Uri.parse(updateURL);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        }
                                    })
                                    .create().show();
                            Looper.loop();
                            break;
                        }
                        else{
                            isAllowToUse=false;
                            Looper.prepare();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("发现了新的版本" + versionName)
                                    .setMessage("您当前的版本过旧，若拒绝更新将无法继续使用！\r\n更新日志：\r\n" + updateLog)
                                    .setCancelable(false)
                                    .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Uri uri = Uri.parse(updateURL);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(intent);
                                        }
                                    })
                                    .create().show();
                            Looper.loop();
                            break;
                        }
                    }
                } catch (java.net.ConnectException e) {
                    //e.printStackTrace();
                    //continue;
                } catch (Exception e) {
                    //e.printStackTrace();
                    break;
                }
            }
        }
    };


//    public boolean handleRecMsg(String[] strArr){
//        switch (strArr[0]){
//            case "LICENSE_CORRECT":
//                insidePort=Integer.valueOf(strArr[1]);
//                validUser=strArr[2];
//                userNum=Integer.valueOf(strArr[3]);
//                group=edt_Group.getText().toString();
//                setSp("userName",edt_UserName.getText().toString());
//                setSp("password",edt_Password.getText().toString());
//                setSp("group",edt_Group.getText().toString());
//                return true;
//            case "LICENSE_WRONG":
//                showAlert("密码错误","请重新输入用户名和密码");
//                setSp("password",null);
//                setSp("group",null);
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        edt_Password.setText("");
//                        edt_Group.setText("");
//
//                    }
//                });
//                return false;
//            case "USING":
//                showAlert("有人正在登录你的账号","如果这的确是你的账号，请稍后再登录");
//                return false;
//
//            default:
//                showAlert("服务器返回了一个错误的数据","怪我咯");
//                return false;
//
//        }
//    }


    private void showAlert(String title,String msg){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Instance
        btn_Login=(Button)this.findViewById(R.id.btn_Login);
        edt_UserName=(EditText)this.findViewById(R.id.edt_UserName);
        edt_Password=(EditText)this.findViewById(R.id.edt_Password);
        edt_Group=(EditText)this.findViewById(R.id.edt_Group);

        //getSpInfo
        isFirstTime =getSpBool("isFirstTime");
        versionCode=getSpInt("versionCode");

        //checkVersion
        new Thread(checkVersion).start();
        try{
            if(!(getLocalVersionCode()==versionCode)){
                showAlert("当前版本更新日志", getVersionName()+"\r\n"+getResources().getString(R.string.updateLog));
                setSp("versionCode", getLocalVersionCode());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(isFirstTime){

            showAlert("欢迎加入！","请及时反馈bug，谢谢！");
            isFirstTime =false;
            setSp("isFirstTime",false);
        }

        user=new User(getSpStr("userName"),getSpStr("password"),getSpStr("group"));


        if(user.getName()!=null){
            edt_UserName.setText(user.getName());
        }

        if(user.getPassword()!=null){
            edt_Password.setText(user.getPassword());
        }

        if(user.getGroup()!=null){
            edt_Group.setText(user.getGroup());
        }

        btn_Login.setOnClickListener(this);


        //DEBUG
        /*
        Intent intent=new Intent(MainActivity.this,Main2Activity.class);
        intent.putExtra("userNickName","2333");
        intent.putExtra("port",Integer.toString(7500));
        startActivity(intent);
        */
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case(R.id.btn_Login):
                if(!edt_UserName.getText().toString().trim().isEmpty()){
                    if(!edt_Password.getText().toString().trim().isEmpty()){
                        if(!edt_Group.getText().toString().trim().isEmpty()){
                            new Thread(checkIfConnected).start();
                        }
                        else{
                            showAlert("","请输入群组");
                        }
                    }
                    else{
                        showAlert("","请输入密码");
                    }
                }
                else{
                    showAlert("","请输入用户名");
                }
                break;
        }
    }

    private void setSp(String index,String str){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(index,str);
        editor.commit();
    }

    private void setSp(String index,boolean bool){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(index,bool);
        editor.commit();
    }

    private void setSp(String index,int i){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(index,i);
        editor.commit();
    }

    private String getSpStr(String index){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPreferences.getString(index,null);
    }

    private Boolean getSpBool(String index){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(index,true);
    }
    private int getSpInt(String index){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(index,-1);
    }

    private int getLocalVersionCode() throws Exception{
        PackageManager packageManager=getPackageManager();
        PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
        int versionCode=packageInfo.versionCode;
        Log.d(TAG,"=============================="+versionCode);
        return versionCode;
    }

    private String getVersionName() throws Exception{
        PackageManager packageManager=getPackageManager();
        PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
        String versionName=packageInfo.versionName;
        Log.d(TAG,"=============================="+versionName);
        return versionName;
    }

    private void setBtnEnabled(boolean isEnable){
        if(isEnable){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_Login.setEnabled(true);
                }
            });
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_Login.setEnabled(false);
                }
            });
        }
    }

}


