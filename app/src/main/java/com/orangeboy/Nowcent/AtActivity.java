package com.orangeboy.Nowcent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.orangeboy.Nowcent.R;

import java.util.List;

public class AtActivity extends AppCompatActivity {
    List<String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at);
    }
}
