package com.example.Nowcent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.google.gson.Gson;


public class Client {
    /*
    FLAG:
    1 version check
    2 receive version check



     */

    private static final String TAG = "MainActivity";
    private OutputStream outputStream;
    private Socket socket;
    public Client(Socket socket){
        this.socket=socket;
    }

    public void send(Message message){
        try {
//            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(message.getFlag());
            Log.d(TAG,"send Flag:"+message.getFlag());
            if(message.getMsg()!=null){
                byte[] bytes = message.getMsg().getBytes();
                dataOutputStream.writeInt(4+4+bytes.length);//Length
                Log.d(TAG,"send Length:"+(4+4+bytes.length));
                dataOutputStream.write(bytes);
                Log.d(TAG,"send Msg:"+message.getMsg());
            }
            else{
                dataOutputStream.writeInt(8); //Length
                Log.d(TAG,"send Length:"+8);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

//    public void send(int flag,Object object){
//        try {
//            InputStream inputStream = socket.getInputStream();
//            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
//            objectOutputStream.write(flag);
//            objectOutputStream.writeObject(new Gson().toJson(object));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    /*
    public void send(int flag, String msg){
        try {
            //Log.d(TAG,"Send:"+msg);
            //PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
            //pw.printf(msg);


            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */


    public Message get(){
        try{
            InputStream inputStream=socket.getInputStream();
            DataInputStream dataInputStream=new DataInputStream(inputStream);
//            byte[] bytes=new byte[1024];
//            dataInputStream.read(bytes);
//            Log.d(TAG,new String(bytes));

            int flag=dataInputStream.readInt();
            Log.d(TAG,"get Flag:"+flag);
            int length=dataInputStream.readInt();
            Log.d(TAG,"get Length:"+length);
            if(length>10000){
                return null;
            }
            byte[] data=new byte[length-4-4];
            dataInputStream.readFully(data);
//            inputStream.close();
            String str=new String(data);
            Log.d(TAG,"get Msg:"+str);
            return new Message(flag,str);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }



    public static User JsonToUser(String string){
        return new Gson().fromJson(string,User.class);
    }

    public static String UserToJson(User user){
        return new Gson().toJson(user);
    }

    public static UserMessage JsonToUserMessage(String string){
        return new Gson().fromJson(string,UserMessage.class);
    }

    public static String UserMessageToJson(UserMessage userMessage){

        return new Gson().toJson(userMessage);
    }

    public static Message_Connect JsonToConnectMessage(String string){
        return new Gson().fromJson(string, Message_Connect.class);
    }

    public static Object JsonToList(String string){
        return new Gson().fromJson(string,Object.class);
    }

}
