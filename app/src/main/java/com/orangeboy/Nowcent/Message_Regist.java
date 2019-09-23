package com.orangeboy.Nowcent;

public class Message_Regist {
    public String user;
    public String nickname;
    public String password;
    public String email;
    public String code;

    public Message_Regist(String user,String nickname,String password,String email){
        this.user=user;
        this.nickname=nickname;
        this.password=password;
        this.email=email;
    }

    public Message_Regist(String user,String nickname,String password,String email,String code){
        this.user=user;
        this.nickname=nickname;
        this.password=password;
        this.email=email;
        this.code=code;
    }

}
