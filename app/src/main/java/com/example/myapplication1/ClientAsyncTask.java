package com.example.myapplication1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ClientAsyncTask extends AsyncTask<Void, byte[], Void> {


    private final int PORT = 8988;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private WifiP2pInfo info;
    private DatagramSocket socket = null;
    private AudioRecord Recorder;
    private AudioTrack audioTrack;
    private InetAddress OwnerAddress;
    private InetAddress ClientAddress;
    private Context mainActivity;
    ClientAsyncTask(Context mainActivity,  WifiP2pInfo info) {
        this.info = info;
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            OwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());

            if (socket == null){
                socket = new DatagramSocket(PORT);
            }


            if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) mainActivity, new String[]{Manifest.permission.RECORD_AUDIO}, 1023 );
            }
            Recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            // group owner일 경우
            if (info.isGroupOwner){
                socket.receive(packet);
                System.out.println("recv msg: "+ new String(packet.getData()).trim());
                ClientAddress = InetAddress.getByName(new String(packet.getData()).trim());
                packet.setAddress(ClientAddress);
            }
            // client일 경우
            else{
                sendClientIPAdressToServer(); // server에 IP 주소 보내기
                packet.setAddress(OwnerAddress);
            }

            Recorder.startRecording();
            while (true){
                Recorder.read(buf, 0, buf.length);
                packet.setData(buf);
                socket.send(packet);

                socket.receive(packet);
                if (audioTrack == null){
                    int bufferS = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT);
                    System.out.println("[bufferSize] "+ bufferS);
                    audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AUDIO_FORMAT, bufferS, AudioTrack.MODE_STREAM);
                    audioTrack.play();
                }
                audioTrack.write(packet.getData(), 0, packet.getData().length);

            }

        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return null;
        }
        finally {
            socket.close();
            socket = null;
        }
    }


    private void sendClientIPAdressToServer(){

        try{
            socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostAddress(), PORT));
            String address = socket.getLocalAddress().getHostAddress();
            DatagramPacket packet = new DatagramPacket(address.getBytes(), address.getBytes().length);
            System.out.println("send IP: "+address);
            socket.send(packet);
            socket.disconnect();
        }
        catch (IOException e){
            System.out.println("[sendClientIPAdressToServer()] "+e.getMessage());
        }
    }
}
