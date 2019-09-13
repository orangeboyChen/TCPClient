package com.example.Nowcent;

public class ConnectMessage {
    private String name;
    private int type;
    public ConnectMessage(String name,int type){
        this.name=name;
        this.type=type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
}
