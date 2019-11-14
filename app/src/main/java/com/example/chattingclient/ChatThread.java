package com.example.chattingclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ChatThread extends Thread
{
  ChatServer myServer;	// ChatServer 객체
  Socket mySocket;		// 클라이언트 소켓

  PrintWriter out;		// 입출력 스트림
  BufferedReader in;
  String keyName;

  public ChatThread(ChatServer server, Socket socket)	// 생성자
  {
    super("ChatThread");

    myServer= server;
    mySocket= socket;

    out= null;
    in= null;
  }

  public void sendMessage(String msg) throws IOException	// 메시지를 전송
  {
    out.println(msg);
    out.flush();
  }

  public void disconnect()	// 연결을 종료
  {
    try{
      out.flush();
      in.close();
      out.close();
      mySocket.close();
      myServer.removeClient(keyName, this);
    }catch(IOException e){
      System.out.println(e.toString());
    }
  }

  public void run()	// 쓰레드 시작
  {					
    try{
      // 소켓을 이용하여 상대방과 입출력 스트림을 생성
      //out= new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream(), "KSC5601"), true);
	  //in= new BufferedReader(new InputStreamReader(mySocket.getInputStream(), "KSC5601"), 1024);
	     
	  out= new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream(), true);
	  in= new BufferedReader(new InputStreamReader(mySocket.getInputStream());
	     
      while(true){	// 클라이언트 보낸 메시지를 처리하기 위해 기다림
    	
        String inLine= in.readLine();	// 기다리고 있다가 클라이언트가 보낸 메시지가 있는 경우 읽어들임
        if(!inLine.equals("") && !inLine.equals(null)){
          messageProcess(inLine);		// 클라이언트가 보낸 메시지를 확인하여 현재 접속한 모든 클라이언트에게 브로드캐스트
        }
      }
    }catch(Exception e){
      disconnect();
    }
  }

  // 클라이언트가 보낸 메시지를 확인한 후 처리
  public void messageProcess(String msg)
  {
    System.out.println(msg);	// 화면에 출력

    StringTokenizer st= new StringTokenizer(msg, "|");	// 규칙에 따라 받은 메시지를 분리하여 확인
    String command= st.nextToken();		// 메시지의 명령 command 부분
    String talk= st.nextToken();		// 메시지의 대화 talk 부분

    if(command.equals("LOGIN")){		// 받은 메시지가 LOGIN 이면 처음 접속 메시지이기 때문에 접속 처리
      System.out.println("[접속] "+ mySocket);
      try{								// 새로운 클라이언트가 접속하여 추가된 클라이언트 수를 브로드캐스트
    	  keyName = talk.trim();
    	  System.out.println("로그인 대화명 저장:" + keyName);
    	  myServer.addClient(keyName, this);
    	  myServer.broadcast("[현재 접속자수] "+ myServer.clientNum +"명");
      }catch(IOException e){
        System.out.println(e.toString());
      }
    }else if(command.equals("LOGOUT")){	// 받은 메시지가 LOGOUT 이면 종료 메시지이므로 제거된 클라이언트의 수를
      try{                              // 브로드캐스트
        myServer.clientNum--;
        myServer.broadcast("[접속 종료] "+ talk);
        myServer.broadcast("[현재 접속자수] "+ myServer.clientNum +"명");
        System.out.println("[현재 접속자수] "+ myServer.clientNum +"명");
      }catch(IOException e){
        System.out.println(e.toString());
      }
      disconnect();						// 연결 종료
    }else if(command.equals("WHISPER")) {	// 귓속말 기능 추
        try{	  // LOGIN, LOGOUT 이외의 경우는 일반 메시지로 모든 클라이언트에게 받은 메시지 전송
        	String tmp1[] = talk.split(":");	// 보낸 사람
        	String tmp2[] = tmp1[1].split(">");
        	System.out.println("귓속말 상대:" + tmp2[0] + "내용:" + tmp2[1]);
            myServer.unicast(tmp2[0],tmp1[0]+"으로부터 귓속말:" + tmp2[1]);
          }catch(IOException e){
            System.out.println(e.toString());
          }
    }    
    else{	
      try{	  // LOGIN, LOGOUT 이외의 경우는 일반 메시지로 모든 클라이언트에게 받은 메시지 전송
        myServer.broadcast(talk);
      }catch(IOException e){
        System.out.println(e.toString());
      }
    }
  }
}