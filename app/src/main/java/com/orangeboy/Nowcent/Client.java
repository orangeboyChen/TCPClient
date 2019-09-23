package com.orangeboy.Nowcent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.util.Base64;


public class Client {
    /*
    FLAG:
    1 version check
    2 receive version check
     */
    private static final String password="32454323";
    private static final String TAG = "LoginActivity";
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
//            Log.d(TAG,"send Flag:"+message.getFlag());
            if(message.getMsg()!=null){
                byte[] bytes = Encrypt(message.getMsg()).getBytes("UTF-8");
                dataOutputStream.writeInt(4+4+bytes.length);//Length
//                Log.d(TAG,"send Length:"+(4+4+bytes.length));
                dataOutputStream.write(bytes);
                Log.d(TAG,"send Msg:"+message.getMsg());
            }
            else{
                dataOutputStream.writeInt(8); //Length
//                Log.d(TAG,"send Length:"+8);

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
//            Log.d(TAG,"get Length:"+length);
            if(length>1000000000){
                return null;
            }
            byte[] data=new byte[length-4-4];
            dataInputStream.readFully(data);
//            inputStream.close();
            String str=Decrypt(new String(data));
            if(!str.equals("")){
                Log.d(TAG,"get Msg:"+str);
            }
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

    public static Message_Version JsonToVersionMessage(String string){
        return new Gson().fromJson(string, Message_Version.class);
    }

    public static List<String> JsonToListStrs(String string){
        return new Gson().fromJson(string,new TypeToken<ArrayList<String>>(){}.getType());
    }

    public static List<UserMessage> JsonToListUserMessage(String string){
        return new Gson().fromJson(string,new TypeToken<ArrayList<UserMessage>>(){}.getType());
    }

    public static String MessageCloudToJson(Message_Cloud message_cloud){
        return new Gson().toJson(message_cloud);
    }

    public static String MessageRegistToJson(Message_Regist message_cloud){
        return new Gson().toJson(message_cloud);
    }

    public String Encrypt(String str){
//        try{
//            IvParameterSpec ivParameterSpec=new IvParameterSpec(password.getBytes("UTF-8"));
//            SecureRandom secureRandom=new SecureRandom();
//            DESKeySpec desKeySpec=new DESKeySpec(password.getBytes("UTF-8"));
//
//            SecretKeyFactory secretKeyFactory=SecretKeyFactory.getInstance("DES");
//            SecretKey secretKey=secretKeyFactory.generateSecret(desKeySpec);
//
//            Cipher cipher=Cipher.getInstance("DES/CBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE,secretKey,ivParameterSpec);
//            return new String(cipher.doFinal(str.getBytes("UTF-8")));
//        }catch(Exception e){
//            e.printStackTrace();
//            return null;
//        }
        return new String(Base64.encode(str.getBytes(),Base64.DEFAULT));
    }

    public String Decrypt(String str){
//        try {
//            IvParameterSpec ivParameterSpec=new IvParameterSpec(password.getBytes("UTF-8"));
//            SecureRandom secureRandom = new SecureRandom();
//            DESKeySpec desKeySpec = new DESKeySpec(password.getBytes("UTF-8"));
//
//            SecretKeyFactory secretKeyFactory=SecretKeyFactory.getInstance("DES");
//            SecretKey secretKey=secretKeyFactory.generateSecret(desKeySpec);
//
//            Cipher cipher=Cipher.getInstance("DES/CBC/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE,secretKey,ivParameterSpec);
//            return new String(cipher.doFinal(str.getBytes("UTF-8")));
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
        return new String(Base64.decode(str.getBytes(),Base64.DEFAULT));
    }

}
