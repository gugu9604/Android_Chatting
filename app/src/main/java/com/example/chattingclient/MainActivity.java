package com.example.chattingclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    EditText serverIp, chatName;
    Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverIp = findViewById(R.id.serverIp);
        chatName = findViewById(R.id.chatName);
        connectBtn = findViewById(R.id.connectBtn);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(getApplicationContext(), "서버에 접속중입니다. ", Toast.LENGTH_LONG).show();
                    Intent it = new Intent(getApplicationContext(), ChatActivity.class);
                    it.putExtra("serverIp", serverIp.getText().toString());
                    it.putExtra("chatName", chatName.getText().toString());
                    startActivity(it);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "서버에 접속할 수 없습니다. ", Toast.LENGTH_LONG).show();
                    Log.i("Connection Error", e.toString());
                }
            }
        });

    }
}