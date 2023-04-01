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
import java.net.UnknownHostException;

public class ClientAsyncTask extends AsyncTask<Void, byte[], Void> {


    private final int PORT = 8988;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private WifiP2pInfo info;
    private static DatagramSocket socket = null;
    private AudioRecord Recorder;
    private AudioTrack audioTrack;
    private InetAddress OwnerAddress;
    private InetAddress ClientAddress;
    private Context mainActivity;
    ClientAsyncTask(Context mainActivity, WifiP2pInfo info) {
        this.info = info;
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            OwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());

            // socket이 null이면 sokcet 생성
            if (socket == null){
                socket = new DatagramSocket(PORT);
            }

            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            // group owner일 경우
            if (info.isGroupOwner){
                receiveClientIpAddress(packet);
                packet.setAddress(ClientAddress);
            }
            // client일 경우
            else{
                sendClientIPAddress(); // server에 IP 주소 보내기
                packet.setAddress(OwnerAddress);
            }

            //Audio 권한 체크
            if (ActivityCompat.checkSelfPermission(MainActivity.getMainActivityContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((MainActivity) MainActivity.getMainActivityContext(), new String[]{Manifest.permission.RECORD_AUDIO}, 1023 );
            }
            // 코드 수정 필요. audio 권한이 체크될 때만 실행.
            else if (ActivityCompat.checkSelfPermission(MainActivity.getMainActivityContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
                Recorder.startRecording();
                while (true){
                    Recorder.read(buf, 0, buf.length);
                    packet.setData(buf);
                    socket.send(packet);

                    socket.receive(packet);
                    if (audioTrack == null){
                        audioTrack = new AudioTrack(
                                AudioManager.STREAM_VOICE_CALL,
                                SAMPLE_RATE,
                                AudioFormat.CHANNEL_OUT_MONO,
                                AUDIO_FORMAT,
                                BUFFER_SIZE,
                                AudioTrack.MODE_STREAM
                        );
                        audioTrack.play();
                    }
                    audioTrack.write(packet.getData(), 0, packet.getData().length);
                }
            }
            return null;
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return null;
        }
        finally {
            if(socket != null){
                socket.close();
                socket = null;
            }
            if (Recorder != null && Recorder.getRecordingState() == Recorder.RECORDSTATE_RECORDING){
                Recorder.stop();
                Recorder = null;
            }
            if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                audioTrack.stop();
                audioTrack = null;
            }
        }
    }


    private void sendClientIPAddress(){
        try{
            socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostAddress(), PORT));
            String address = socket.getLocalAddress().getHostAddress();
            DatagramPacket packet = new DatagramPacket(address.getBytes(), address.getBytes().length);
            System.out.println("send IP: "+address);
            socket.send(packet);
            socket.disconnect();
        }
        catch (IOException e){
            System.out.println("[sendClientIPAddress] "+e.getMessage());
        }
    }

    private void receiveClientIpAddress(DatagramPacket packet){
        try{
            socket.receive(packet);
            System.out.println("recv msg: "+ new String(packet.getData()).trim());
            ClientAddress = InetAddress.getByName(new String(packet.getData()).trim());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




}
