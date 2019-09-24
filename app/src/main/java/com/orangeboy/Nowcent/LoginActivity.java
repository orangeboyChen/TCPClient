package com.orangeboy.Nowcent;

import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

public class LoginActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    //Variables
    private int versionCode;
    private boolean isAllowThread = true;
    private boolean isAllowToUse=true;
    private boolean isFirstTime = true;
    private static final String TAG = "LoginActivity";//Log
    //Object
    private Button btn_Login;
    private Button btn_new;
    private EditText edt_UserName;
    private EditText edt_Password;
    private EditText edt_Group;
    private Socket socket;
    private Client client;
    private Message message;
    private User user;

    private ProgressDialog progressDialog;

    Runnable checkIfConnected = new Runnable() {
        @Override
        public void run() {
            if (!isAllowToUse) {
                progressDialog.dismiss();
                new Thread(checkVersion).start();
            } else {
                try {
                    Thread.sleep(400);
                    setBtnEnabled(false);//set button

                    //set socket
                    SocketAddress socketAddress = new InetSocketAddress(getResources().getString(R.string.ip), 6000);
                    socket = new Socket();
                    socket.connect(socketAddress, 300);
                    client = new Client(socket);
                    client.send(new Message(FLAG.LOGIN));

                    progressDialog.dismiss();

                    while (isAllowThread) {
                        Message loginMessage = client.get();
                        //Log.d(TAG, "str:" + str);
                        if (loginMessage.getFlag()==FLAG.ALLOW) {
                            break;
                        } else if (loginMessage.getFlag()==FLAG.REFUSE) {
                            showAlert("你已被禁止登录", "请联系管理员");
                            //socket.close();
                            break;
                        }
                    }
                    setBtnEnabled(true);
                    //Log.d(TAG,"33333333333333333333333");
                    client.send(new Message(FLAG.CHECK_USER,Client.UserToJson(new User(edt_UserName.getText().toString(),edt_Password.getText().toString(),edt_Group.getText().toString()))));
                    Message checkMessage = client.get();
                    if(checkMessage.getFlag()==FLAG.CHECK_USER){
                        user=Client.JsonToUser(checkMessage.getMsg());
                        //set Sp
                        setSp("userName",user.getName());
                        setSp("password",user.getPassword());
                        setSp("group",user.getGroup());

                        //Prepare activity
                        Intent intent = new Intent()
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                .setClass(LoginActivity.this, ChatActivity.class)
                                .putExtra("user",Client.UserToJson(user));
                        isAllowThread = false;
                        socket.close();
                        startActivity(intent);
                    }
                    else if(checkMessage.getFlag()==FLAG.LOGIN_ERROR){
                        setBtnEnabled(true);
                        showAlert("密码错误","请重新输入用户名和密码");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                edt_Password.setText("");
                                edt_Group.setText("");
                            }
                        });
                        //Set Sp
                        setSp("password",null);
                        setSp("group",null);

                    }
                    else if(checkMessage.getFlag()==FLAG.LOGIN_USING){
                        setBtnEnabled(true);
                        showAlert("有人正在登录你的账号","如果这的确是你的账号，请稍后再登录");
                    }
                    else {
                        setBtnEnabled(true);
                        showAlert("连接失败", "服务器返回了错误的数据");
                    }
                } catch (java.net.SocketTimeoutException e) {
                    progressDialog.dismiss();
                    setBtnEnabled(true);
                    showAlert("连接失败", "请稍后再试");
                } catch (java.net.ConnectException e) {
                    progressDialog.dismiss();
                    setBtnEnabled(true);
                    showAlert("连接失败", "请检查你的网络连接");
                } catch (Exception e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
    };


    Runnable checkVersion = new Runnable() {
        @Override
        public void run() {
            while (isAllowThread) {
                try{
                    Socket socket = new Socket(getResources().getString(R.string.ip), 6002);
                    Client client = new Client(socket);

                    int localVersionCode= getLocalVersionCode();

                    client.send(new Message(FLAG.VERSION));
                    message=client.get();
                    final Message_Version message_version;
                    if(message.getFlag()==FLAG.VERSION){
                        message_version=Client.JsonToVersionMessage(message.getMsg());
                    }
                    else{
                        continue;
                    }
                    if(localVersionCode>=message_version.getNewVersionCode()){
                        break;
                    }
                    else{
                        if(localVersionCode>=message_version.getMinVersionCode()){
                            //Show dialog
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle("发现了新的版本" + message_version.getVersionName())
                                            .setMessage("更新日志：\r\n" + message_version.getUpdateLog())
                                            .setCancelable(false)
                                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            })
                                            .setNegativeButton("下载该版本", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Uri uri = Uri.parse(message_version.getUpdateURL());
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(intent);
                                                }
                                            })
                                            .create().show();
                                }
                            });
                            break;
                        }
                        else{
                            isAllowToUse=false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle("发现了新的版本" + message_version.getVersionName())
                                            .setMessage("您当前的版本过旧，若拒绝更新将无法继续使用！\r\n更新日志：\r\n" + message_version.getUpdateLog())
                                            .setCancelable(false)
                                            .setPositiveButton("好", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Uri uri = Uri.parse(message_version.getUpdateURL());
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    startActivity(intent);
                                                }
                                            })
                                            .create().show();
                                }
                            });

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




    private void showAlert(String title,String msg){
//        try{
//            Looper.prepare();
//        }catch (Exception e){}
        final AlertDialog.Builder builder= new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create();
                builder.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Instance
        btn_Login=this.findViewById(R.id.btn_Login);
        btn_new=this.findViewById(R.id.btn_new);
        edt_UserName=this.findViewById(R.id.edt_UserName);
        edt_Password=this.findViewById(R.id.edt_Password);
        edt_Group=this.findViewById(R.id.edt_Group);

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
        btn_new.setOnClickListener(this);
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_Login:
                if(!edt_UserName.getText().toString().trim().isEmpty()){
                    if(!edt_Password.getText().toString().trim().isEmpty()){
                        if(!edt_Group.getText().toString().trim().isEmpty()){
                            showProgressDialog("正在连接");
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
            case R.id.btn_new:
                Intent intent = new Intent()
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        .setClass(LoginActivity.this, RegistActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void setSp(String index,String str){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(index,str);
        editor.apply();
    }

    private void setSp(String index,boolean bool){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(index,bool);
        editor.apply();
    }

    private void setSp(String index,int i){
        SharedPreferences sharedPreferences=getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(index,i);
        editor.apply();
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
        Log.d(TAG,"Version Code:"+versionCode);
        return versionCode;
    }

    private String getVersionName() throws Exception{
        PackageManager packageManager=getPackageManager();
        PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
        String versionName=packageInfo.versionName;
        Log.d(TAG,"Version Name:"+versionName);
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

    private void showProgressDialog(String str){
        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(str);
        progressDialog.show();
    }

}


