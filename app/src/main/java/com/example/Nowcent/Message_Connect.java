package com.example.Nowcent;

public class Message_Connect {
    private String name;
    private int type;
    public Message_Connect(String name, int type){
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
