package com.orangeboy.Nowcent;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class RegistActivity extends AppCompatActivity implements View.OnClickListener {
    private int flag;

    private TextView txv_PasswordInfo;
    private EditText edt_Name;
    private EditText edt_Nickname;
    private EditText edt_Password;
    private EditText edt_rePassword;
    private EditText edt_Email;
    private EditText edt_Code;
    private Button btn_GetCode;
    private Button btn_Regist;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        //Get instance
        txv_PasswordInfo=this.findViewById(R.id.txv_passwordInfo);
        edt_Name=this.findViewById(R.id.edt_regist_name);
        edt_Nickname=this.findViewById(R.id.edt_regist_nickname);
        edt_Password=this.findViewById(R.id.edt_regist_password);
        edt_rePassword=this.findViewById(R.id.edt_regist_repassword);
        edt_Email=this.findViewById(R.id.edt_regist_email);
        edt_Code=this.findViewById(R.id.edt_regist_code);
        btn_GetCode=this.findViewById(R.id.btn_regist_getCode);
        btn_Regist=this.findViewById(R.id.btn_regist);

        txv_PasswordInfo.setVisibility(View.GONE);

        //Set OnClickListener and TextWatcher
        btn_GetCode.setOnClickListener(this);
        btn_Regist.setOnClickListener(this);

        edt_Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!edt_Password.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@&%#_])[a-zA-Z0-9~!@&%#_]{8,16}$")){
                    txv_PasswordInfo.setVisibility(View.VISIBLE);
                }
                else{
                    txv_PasswordInfo.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edt_Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(!edt_Password.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@&%#_])[a-zA-Z0-9~!@&%#_]{8,16}$")){
                        txv_PasswordInfo.setVisibility(View.VISIBLE);
                    }
                    else{
                        txv_PasswordInfo.setVisibility(View.GONE);
                    }
                }
            }
        });


    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_regist_getCode:
                if(checkCanRegist()) {
                    flag = FLAG.REGIST_REQUEST;
                    showProgressDialog("正在获取验证码");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_GetCode.setEnabled(false);
                        }
                    });
                    new Thread(connectRunnable).start();
                }
                break;
            case R.id.btn_regist:
                if(checkIfValid()) {
                    showProgressDialog("正在连接");
                    flag = FLAG.REGIST;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_Regist.setEnabled(false);
                        }
                    });
                    new Thread(connectRunnable).start();
                }
                    break;
        }

    }

    private boolean checkIfValid(){
        if(!edt_Name.getText().toString().trim().isEmpty()){
            if(!edt_Nickname.getText().toString().trim().isEmpty()){
                if(edt_Password.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@&%#_])[a-zA-Z0-9~!@&%#_]{8,16}$")){
                    if(edt_rePassword.getText().toString().equals(edt_Password.getText().toString())){
                        if(edt_Email.getText().toString().matches("^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$")){
                            if(edt_Code.getText().toString().length()==6){
                                return true;
                            }
                            else{
                                showAlert("","请输入正确的验证码");
                            }
                        }
                        else{
                            showAlert("","请输入正确的邮箱地址");
                        }
                    }
                    else{
                        showAlert("","两次密码输入不一致，请重新输入");
                    }
                }
                else{
                    showAlert("","密码不符合规则，请重新输入");
                }
            }
            else {
                showAlert("", "请输入昵称");
            }
        }
        else{
            showAlert("","请输入用户名");
        }
        return false;
    }

    private boolean checkCanRegist(){
        if(!edt_Name.getText().toString().trim().isEmpty()){
            if(!edt_Nickname.getText().toString().trim().isEmpty()){
                if(edt_Password.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@&%#_])[a-zA-Z0-9~!@&%#_]{8,16}$")){
                    if(edt_rePassword.getText().toString().equals(edt_Password.getText().toString())){
                        if(edt_Email.getText().toString().matches("^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$")){
                            return true;
                        }
                        else{
                            showAlert("","请输入正确的邮箱地址");
                        }
                    }
                    else{
                        showAlert("","两次密码输入不一致，请重新输入");
                    }
                }
                else{
                    showAlert("","密码不符合规则，请重新输入");
                }
            }
            else {
                showAlert("", "请输入昵称");
            }
        }
        else{
            showAlert("","请输入用户名");
        }
        return false;
    }


    Runnable connectRunnable=new Runnable() {
        @Override
        public void run() {
            try {
                SocketAddress socketAddress = new InetSocketAddress(getResources().getString(R.string.ip), 6003);
                Socket socket = new Socket();
                socket.connect(socketAddress, 300);

                Thread.sleep(400);
                progressDialog.dismiss();
                Client client=new Client(socket);
                client.send(new Message(flag,Client.MessageRegistToJson(new Message_Regist(edt_Name.getText().toString(),edt_Nickname.getText().toString(),edt_Password.getText().toString(),edt_Email.getText().toString(),edt_Code.getText().toString()))));
                Message message=client.get();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_GetCode.setEnabled(true);
                        btn_Regist.setEnabled(true);
                    }
                });
                if(message!=null){
                    switch(message.getFlag()){
                        case FLAG.REGIST:
                            showAlert("验证码申请成功","验证码将在三个工作日内发送至您的邮箱。获取验证码后，请输入本次注册的信息再次注册。");
                            break;
                        case FLAG.REGIST_EMAIL_HAS_BEEN_USED:
                            showAlert("验证码申请失败","该邮箱已使用。");
                            break;
                        case FLAG.REGIST_NAMEORNICKNAME_ERROR:
                            showAlert("验证码申请失败","请换一个用户名或昵称。");
                            break;
                        case FLAG.REGIST_REGISTING:
                            showAlert("您的信息正在审核","请稍安勿躁。验证码将在三个工作日内发送至您的邮箱。");
                            break;
                        case FLAG.REGIST_SUCCESSFUL:
                            showAlert("注册成功","敬请享受吧。");
                            break;
                        case FLAG.REGIST_ERROR:
                            showAlert("注册失败","您的信息有误，请重新输入。");
                            break;
                    }
                }

            }catch (Exception e){
                progressDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_GetCode.setEnabled(true);
                        btn_Regist.setEnabled(true);
                    }
                });
                showAlert("连接失败","请稍后再试");


                e.printStackTrace();
            }
        }
    };

    private void showAlert(String title,String msg){
        try {
            Looper.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        new AlertDialog.Builder(RegistActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create().show();
        Looper.loop();
    }

    private void showProgressDialog(String str){
        progressDialog=new ProgressDialog(RegistActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(str);
        progressDialog.show();
    }
}
