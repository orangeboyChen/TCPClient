package com.orangeboy.Nowcent;

public class Message {
    private int flag;
    private String msg;
    public Message(int flag,String msg){
        this.flag=flag;
        this.msg=msg;
    }
    public Message(int flag){
        this.flag=flag;
    }
    public int getFlag(){
        return flag;
    }
    public String getMsg(){
        return msg;
    }
}
