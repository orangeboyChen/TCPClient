package com.example.Nowcent;

public class UserMessage {
    public String user;
    public String time;
    public String msg;
    public String group;
    public String nickName;
    public UserMessage(){

    }
    public UserMessage(String user,String time,String msg,String group){
        this.user=user;
        this.time=time;
        this.msg=msg;
        this.group=group;
    }

    public String getUser() {
        return user;
    }

    public String getTime() {
        return time;
    }

    public String getMsg() {
        return msg;
    }

    public String getGroup() {
        return group;
    }
}
