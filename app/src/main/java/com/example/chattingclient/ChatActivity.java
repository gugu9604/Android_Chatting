package com.example.chattingclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class ChatActivity extends Activity {

    String serverIp;
    String chatName;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    BackgroundTask msgReceive;
    ScrollView sc;
    LinearLayout chatView;
    EditText msgInput;
    Button sendBtn;
    boolean isDead;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent it = getIntent();
        serverIp = it.getStringExtra("serverIp");
        chatName = it.getStringExtra("chatName");

        sc = findViewById(R.id.sc);
        chatView = findViewById(R.id.chatView);

        msgReceive = new BackgroundTask();
        msgReceive.execute("메시지 수신 시작");
        msgInput = findViewById(R.id.msgInput);
        sendBtn = findViewById(R.id.sendBtn);

        // 전송버튼 눌렀을 경우 메시지 전송하기
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = msgInput.getText().toString();
                if(msg.equals("exit")) sendMsg(chatName, msg, 3);
                else sendMsg(chatName, msg, 2);
                msgInput.setText("");

            }
        });

        //엔터키 눌렀을 때 입력 메시지 전송하기
        msgInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i ==  KeyEvent.KEYCODE_ENTER && KeyEvent.ACTION_DOWN == keyEvent.getAction())
                {
                    String msg = msgInput.getText().toString();
                    if(msg.equals("exit")) sendMsg(chatName, msg, 3);
                    else sendMsg(chatName, msg, 2);
                    msgInput.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    public void sendMsg(String chatName, String msg, int msgType) {
        // 형식에 맞춰 서버에 메시지를 전송
        if(msgType == 1) {
            out.println("LOGIN|" + chatName);
        }
        else if(msgType == 2) {
            out.println("TALK|" + "[" + chatName + "]" + ":" + msg);
        }
        else if(msgType == 3) {
            out.println("LOGOUT|" + chatName);
            isDead = true;
        }
        out.flush();
    }

    class BackgroundTask extends AsyncTask<String , String , Integer> {
        String msg;
        protected void onPreExecute() {
            TextView tv = new TextView(getApplicationContext());
            tv.setTextColor(Color.RED);
            tv.setText("연결중입니다.");
            chatView.addView(tv);
        }

        protected Integer doInBackground(String ... value) {
            // 초기 서버에 접속한 후 접속 메시지를 보냄
            try {
                socket = new Socket(serverIp, 14001);
                out= new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "KSC5601"), true);
                in= new BufferedReader(new InputStreamReader(socket.getInputStream(), "KSC5601"), 1024);
                //out = new PrintWriter(socket.getOutputStream(), true);
                //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch(Exception e) {
                Log.i("Connection Error", "onPreExecute");
                return 0;
            }

            sendMsg(chatName, "", 1);

            while (!isDead) {
                try {
                    msg = in.readLine();            // 상대방이 보낸 메시지를 읽어들임
                    if ((msg != null) && (!msg.equals(""))) {
                        publishProgress(msg);            // 내 화면의 memo에 받은 메시지 출력
                    }
                } catch (Exception e) { }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {}
            }

            return 0;
        }

        protected void onProgressUpdate(String ... msg) {
            TextView tv = new TextView(getApplicationContext());
            if(msg[0].toString().indexOf(chatName)>0) tv.setTextColor(Color.BLUE);
            else tv.setTextColor(Color.BLACK);

            tv.setText(msg[0].toString());
            chatView.addView(tv);

            /*  내 메시지는 오른쪽 끝으로 정렬할 경우
            if(msg[0].toString().indexOf(chatName)>0) {
                tv.setTextColor(Color.BLUE);
                tv.setText(msg[0].toString());
                LinearLayout tp = new LinearLayout(getApplicationContext());
                tp.setGravity(Gravity.RIGHT);
                tp.addView(tv);
                chatView.addView(tp);
            }
            else {
                tv.setTextColor(Color.BLACK);
                tv.setText(msg[0].toString());
                chatView.addView(tv);
            }
            */
            sc.fullScroll(View.FOCUS_DOWN);
        }

        protected void onPostExecute(Integer result) {
            TextView tv = new TextView(getApplicationContext());
            tv.setTextColor(Color.RED);
            tv.setText("연결을 종료합니다.");
            chatView.addView(tv);
            finish();
        }
    }

    @Override
    protected void onStop() {
        try {
            if(socket.isConnected()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch(Exception e) { }
        super.onStop();
    }
}
