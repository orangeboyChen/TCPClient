package com.orangeboy.Nowcent;

public class UserMessage {
    static final int MSG = 1;
    static final int IMG = 2;


    public int type;
    public String user;
    public String time;
    public String msg;
    public String group;
    public String nickName;
    public String atUser;
    public int imgId;
    public int messageId;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }


    public UserMessage(int type,String user,String time,String msg,String group,String nickName,String atUser,int imgId,int messageId)
    {
        this.type = type;
        this.user = user;
        this.msg = msg;
        this.group = group;
        this.nickName = nickName;
        this.atUser = atUser;
        this.imgId = imgId;
        this.messageId = messageId;
        this.time = time;
    }





    public UserMessage(){
    }
    public UserMessage(String user,String msg,String group,int type){
        this.user=user;
        this.msg=msg;
        this.group=group;
        this.type=type;
    }

//    public UserMessage(String user,int imgId,String group,int type){
//        this.user=user;
//        this.imgId=imgId;
//        this.group=group;
//        this.type=type;
//    }

    public UserMessage(String user,String msg,int imgId,String group,int type){
        this.user=user;
        this.msg=msg;
        this.imgId=imgId;
        this.group=group;
        this.type=type;

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

    public int getType() {
        return type;
    }

    public int getImgId() {
        return imgId;
    }

    public String getNickName(){
        return nickName;
    }
    public String getAtUser(){
        return atUser;
    }

}
