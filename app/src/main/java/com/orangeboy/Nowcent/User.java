package com.orangeboy.Nowcent;

public class User {
    public String name;
    public String password;
    public String group;
    public String nickName;
    public int port;
    public int userNum;
    public User(String name,String password,String group){
        this.name=name;
        this.password=password;
        this.group=group;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getGroup() {
        return group;
    }

    public String getNickName() {
        return nickName;
    }

    public int getPort() {
        return port;
    }

    public int getUserNum() {
        return userNum;
    }
}
