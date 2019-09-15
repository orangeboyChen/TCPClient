package com.example.Nowcent;

public class UserImg {
    public String user;
    public String time;
    public int imgId;
    public String group;
    public String nickName;

    public UserImg(String user,int imgId,String group){
        this.user = user;
        this.imgId = imgId;
        this.group=group;
    }

    public String getUser() {
        return user;
    }

    public String getTime() {
        return time;
    }

    public int getImgId() {
        return imgId;
    }

    public String getGroup() {
        return group;
    }

    public String getNickName() {
        return nickName;
    }




}
