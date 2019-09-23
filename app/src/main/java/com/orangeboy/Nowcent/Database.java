package com.orangeboy.Nowcent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    private static Database database;
    private static SQLiteDatabase sqLiteDatabase;

    public static SQLiteDatabase getSqLiteDatabase(Context context){
        database=new Database(context,"NowCent",null,1);
        sqLiteDatabase=database.getWritableDatabase();
        return sqLiteDatabase;
    }
    public static SQLiteDatabase getSqLiteDatabase(){
        return sqLiteDatabase;
    }


    private Database(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory,int version){
        super(context,name,cursorFactory,version);
    }

    public static Database getDatabase(){
        return database;
    }
    @Override
    public void onCreate(SQLiteDatabase database){
//        database.execSQL("create table UserMessages(Time nvarchar(50),[User] nvarchar(50),UserNickName nvarchar(50),[Group] nvarchar(100),Type int,Msg nvarchar(4000),ImgId int,MessageId int,AtUser nvarchar(50))");
        database.execSQL("create table UserMessages(Time text,[User] text,UserNickName text,[Group] text,Type int,Msg text,ImgId int,MessageId int,AtUser text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){

    }

    public static class UserMessagesDatabase{
        public static List<UserMessage> getUserMessagesList(){
            Cursor cursor=getSqLiteDatabase().query("UserMessages",new String[]{"Time","User","UserNickName","[Group]","Type","Msg","ImgId","MessageId","AtUser"},null,null,null,null,null);
            List<UserMessage> list=new ArrayList<>();
            while (cursor.moveToNext()){
                String time=cursor.getString(cursor.getColumnIndex("Time"));
                String user=cursor.getString(cursor.getColumnIndex("User"));
                String userNickName=cursor.getString(cursor.getColumnIndex("UserNickName"));
                String group=cursor.getString(cursor.getColumnIndex("Group"));
                int type=cursor.getInt(cursor.getColumnIndex("Type"));
                String msg=cursor.getString(cursor.getColumnIndex("Msg"));
                int imgId=cursor.getInt(cursor.getColumnIndex("ImgId"));
                int messageId=cursor.getInt(cursor.getColumnIndex("MessageId"));
                String atUser=cursor.getString(cursor.getColumnIndex("AtUser"));
                list.add(new UserMessage(type,user,time,msg,group,userNickName,atUser,imgId,messageId));
            }
            cursor.close();
            return list;
        }

        public static void addUserMessageToDatabase(UserMessage userMessage){
            ContentValues contentValues=new ContentValues();
            contentValues.put("Time",userMessage.getTime());
            contentValues.put("[User]",userMessage.getUser());
            contentValues.put("UserNickName",userMessage.getNickName());
            contentValues.put("[Group]",userMessage.getGroup());
            contentValues.put("Type",userMessage.getType());
            contentValues.put("Msg",userMessage.getMsg());
            contentValues.put("ImgId",userMessage.getImgId());
            contentValues.put("MessageId",userMessage.getMessageId());
            contentValues.put("AtUser",userMessage.getAtUser());
            getSqLiteDatabase().insert("UserMessages",null,contentValues);

        }

        public static void addUserMessagesListToDatabase(List<UserMessage> list){
            Log.d("LoginActivity","qidong");
            for(int i=0;i<list.size();i++){
                addUserMessageToDatabase(list.get(i));
                Log.d("LoginActivity","forxunhuan");
            }
        }
    }



}
