package com.orangeboy.Nowcent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter {
    ArrayList<ListItem> list;
    private LayoutInflater layoutInflater;

    public static final int USER_MESSAGE=0;
    public static final int SYSTEM_MESSAGE=1;
    public static final int USER_IMG=2;
    public static final int VIEWHOLDER_COUNT=3;


    public Adapter(Context context, ArrayList<ListItem> list){
        this.list=list;
        layoutInflater=LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return VIEWHOLDER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        ListItem listItem=list.get(position);
        return listItem.getType();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder1 viewHolder1=null;
        ViewHolder2 viewHolder2=null;
        ViewHolder3 viewHolder3=null;
        View view=null;
        int type=getItemViewType(i);
        if(convertView==null){
            switch (type){
                case USER_MESSAGE:
                    viewHolder1=new ViewHolder1();
                    view=layoutInflater.inflate(R.layout.listview,null);
                    viewHolder1.txv_User=(TextView)view.findViewById(R.id.txv_user);
                    viewHolder1.txv_Time=(TextView)view.findViewById(R.id.txv_time);
                    viewHolder1.txv_Msg=(TextView)view.findViewById(R.id.txv_msg);
                    viewHolder1.image=(ImageView)view.findViewById(R.id.img_user);
                    view.setTag(viewHolder1);
                    break;
                case SYSTEM_MESSAGE:
                    viewHolder2=new ViewHolder2();
                    view=layoutInflater.inflate(R.layout.listview_connect,null);
                    view.setTag(viewHolder2);
                    break;
                case USER_IMG:
                    viewHolder3=new ViewHolder3();
                    view=layoutInflater.inflate(R.layout.listview_img,null);
                    viewHolder3.txv_User=(TextView)view.findViewById(R.id.txv_user);
                    viewHolder3.txv_Time=(TextView)view.findViewById(R.id.txv_time);
                    viewHolder3.image_User=(ImageView)view.findViewById(R.id.img_user);
                    viewHolder3.image_img=(ImageView)view.findViewById(R.id.img_emoji);
                    view.setTag(viewHolder3);
            }
        }
        else{
            view=convertView;
            switch(type){
                case USER_MESSAGE:
                    viewHolder1=(ViewHolder1)view.getTag();
                    break;
                case SYSTEM_MESSAGE:
                    viewHolder2=(ViewHolder2)view.getTag();
                    break;
                case USER_IMG:
                    viewHolder3=(ViewHolder3)view.getTag();
            }
        }

        ListItem listItem;
        switch (type){
            case USER_MESSAGE:
                listItem=(ListItem)getItem(i);
                switch (listItem.getName()){
                    case "GPM":
                        viewHolder1.image.setImageResource(R.drawable.gpm_png);
                        break;
                    case "orangeboy":
                        viewHolder1.image.setImageResource(R.drawable.admin_png);
                        break;
                    default:
                        viewHolder1.image.setImageResource(R.drawable.user_png);
                }
                viewHolder1.txv_User.setText(list.get(i).getName());
                viewHolder1.txv_Time.setText(list.get(i).getTime());
                viewHolder1.txv_Msg.setText(list.get(i).getMsg());
                break;
            case SYSTEM_MESSAGE:
                viewHolder2.txv_Msg=(TextView)view.findViewById(R.id.txv_System);
                viewHolder2.txv_Msg.setText(list.get(i).getMsg());
                break;
            case USER_IMG:
                listItem=(ListItem)getItem(i);
                switch (listItem.getName()){
                    case "GPM":
                        viewHolder3.image_User.setImageResource(R.drawable.gpm_png);
                        break;
                    case "orangeboy":
                        viewHolder3.image_User.setImageResource(R.drawable.admin_png);
                        break;
                    default:
                        viewHolder3.image_User.setImageResource(R.drawable.user_png);
                }
                viewHolder3.txv_User.setText(list.get(i).getName());
                viewHolder3.txv_Time.setText(list.get(i).getTime());
                viewHolder3.image_img.setImageResource(list.get(i).getImgId());
        }
        return view;
    }

    class ViewHolder1{
        TextView txv_User;
        TextView txv_Time;
        TextView txv_Msg;
        ImageView image;
    }

    class ViewHolder2{
        TextView txv_Msg;
    }

    class ViewHolder3{
        TextView txv_User;
        TextView txv_Time;
        ImageView image_User;
        ImageView image_img;
    }
}

class ListItem{
    private int type;
    private String name;
    private String time;
    private String msg;
    private int imgId;

    public int getImgId() {
        return imgId;
    }



    public ListItem(UserMessage userMessage){
        switch (userMessage.getType()){
            case UserMessage.MSG:
                this.type=Adapter.USER_MESSAGE;
                break;
            case UserMessage.IMG:
                this.type=Adapter.USER_IMG;
                this.imgId=userMessage.getImgId();
                break;
        }
        this.name=userMessage.getNickName();
        this.time=userMessage.getTime();
        this.msg=userMessage.getMsg();

    }

    public ListItem(Message_Connect message_connect){
        this.type=Adapter.SYSTEM_MESSAGE;
        switch (message_connect.getType()){
            case FLAG.JOIN:
                this.msg=message_connect.getName()+"已加入";
                break;
            case FLAG.RE:
                this.msg="你正在重连";
                break;
        }
    }

    public ListItem(String str){
        this.type=Adapter.SYSTEM_MESSAGE;
        this.msg=str;
    }


    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getMsg() {
        return msg;
    }
}

