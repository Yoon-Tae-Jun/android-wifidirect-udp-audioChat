package com.example.myapplication1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ServerAsyncTask extends AsyncTask<String, Long, String> {
    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            String str = bundle.getString("data");
            text1.setText("recv msg - " + str);
        }
    };

    TextView text1;
    int port;
    ServerSocket serverSocket;
    public ServerAsyncTask(TextView text1, int port){
        this.text1 = text1;
        this.port = port;


    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Socket is open");
            Socket client = serverSocket.accept();
            System.out.println("Socket is connection");

            byte[] bufRcv = new byte[1024];
            byte[] bufSend;
            String msg;
            int size;
            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();

            while (true){
                size = inputStream.read(bufRcv);
                System.out.println("recv msg size: "+size);
                msg = new String(Arrays.copyOfRange(bufRcv,0,size));
                System.out.println("recv msg:"+msg);


                Bundle data = new Bundle();
                data.putString("data",msg);

                Message message = handler.obtainMessage();
                message.setData(data);
                handler.sendMessage(message);


                if(msg.equals("aaa"))
                    break;
            }
            Thread inputThread = new Thread(() -> {

            });
            inputThread.start();

            for (int i = 0; i<10; i++){
                Random rd = new Random();
                msg = "hi i'm server" + Integer.toString(rd.nextInt(45) + 1);
                System.out.println("send0 msg:"+msg);
                bufSend = msg.getBytes();

                outputStream.write(bufSend, 0, bufSend.length);
                System.out.println("send0 msg size:"+bufSend.length);
                outputStream.flush();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            bufSend = "aaa".getBytes();
            outputStream.write(bufSend, 0, bufSend.length);
            outputStream.close();

            serverSocket.close();
            return msg;
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {

    }


}
