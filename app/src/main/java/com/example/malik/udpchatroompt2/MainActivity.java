package com.example.malik.udpchatroompt2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.util.*;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_NAME = "ec2-18-191-220-46.us-east-2.compute.amazonaws.com";
    private static final int SERVER_PORT = 4445;
    private static final int BUFFER_SIZE = 256;

    private static InetAddress mServerAddress;
    private static DatagramSocket mSocket;

    private static final String JOINMESSAGE = "TYPE=JOIN;USERNAME=";
    private static final String JOINRESPONSE_0 = "TYPE=JOINRESPONSE;STATUS=0;TOKEN=";
    private static String token = "";
    private static String username = "";
    private static final String TAG = "HW03";
    private static boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String info1 = "Welcome to HW03Chat! What is your name?";
        String info2 = "Successfully joined HW03Chat!\nEnter 'LEAVE' to exit.";

        try {
            mSocket = new DatagramSocket();
            mServerAddress = InetAddress.getByName(SERVER_NAME);
            ((TextView)findViewById(R.id.textView)).setText(info1);
            while (!ready) {
            }
            username = ((EditText)findViewById(R.id.editText)).getText().toString();
            sendJoinMessage(username);
            DatagramPacket packet = receiveResponse();
            displayResults(packet);
            ((TextView)findViewById(R.id.textView)).setText(info2);
        }
        catch (IOException e) {
            e.printStackTrace();
        }



        MessageSender sdr = new MessageSender(mSocket, SERVER_NAME, token);
        MessageReceiver rvr = new MessageReceiver(mSocket, token);
        Thread sender = new Thread(sdr);
        Thread receiver = new Thread(rvr);
        sender.start();
        receiver.start();
    }

    private void btnJoin(View view) throws IOException {
        ready = true;
    }

    private static void sendJoinMessage(String message) throws IOException {
        message = JOINMESSAGE + message;
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, mServerAddress, SERVER_PORT);
        mSocket.send(packet);
    }

    private static DatagramPacket receiveResponse() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        mSocket.receive(packet);

        InetAddress serverAddress = packet.getAddress();
        int serverPort = packet.getPort();

        return packet;
    }

    private static void displayResults(DatagramPacket packet) {
        String received = new String(packet.getData(), 0, packet.getLength());
        if (received.startsWith(JOINRESPONSE_0)) {
            token = received.substring(JOINRESPONSE_0.length());
        }
    }

    private class MessageSender implements Runnable{
        //private static final String SERVER_NAME = "10.66.54.13";
        private static final String SERVER_NAME = "localhost";
        private static final int SERVER_PORT = 4445;
        private static final String POST = "TYPE=POST;TOKEN=";
        private static final String MESSAGE = ";MESSAGE=";
        private static final String LEAVE = "TYPE=LEAVE;TOKEN=";
        private DatagramSocket mSocket;
        private DatagramPacket packet;
        private InetAddress mServerAddress;
        private String hostname;
        private String message;
        private byte[] buffer;
        private String token;
        private boolean connected = true;
        private boolean readyToSend = false;

        public MessageSender(DatagramSocket s, String h, String t) {
            mSocket = s;
            hostname = h;
            token = t;
        }

        private void btnMessageSend(View view) throws IOException {
            readyToSend = true;
        }

        private String encodeMessage(String message) throws IOException {
            String encodedMsg = "";
            if (message.equals("LEAVE")) {
                encodedMsg = LEAVE + token;
                connected = false;
            }
            else {
                encodedMsg = POST + token + MESSAGE + message;
            }
            return encodedMsg;
        }

        private void deliverMessage(String msg) throws IOException {
            buffer = msg.getBytes();
            Log.d(TAG, "Sending: " + msg);
            mServerAddress = InetAddress.getByName(SERVER_NAME);
            packet = new DatagramPacket(buffer, buffer.length, mServerAddress, SERVER_PORT);
            mSocket.send(packet);
        }

        public void run() {
            while (connected) {
                while(!readyToSend) {
                    message = ((EditText)findViewById(R.id.editText)).getText().toString();
                }
                try {
                    String encodedMessage = encodeMessage(message);
                    deliverMessage(encodedMessage);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                readyToSend = false;
            }
        }
    }

    private class MessageReceiver implements Runnable{
        private static final int BUFFER_SIZE = 256;
        private DatagramSocket mSocket;
        private byte[] mPacketBuffer;
        private String token;

        private static final String NEWMESSAGE = "TYPE=NEWMESSAGE;USERNAME=";
        private static final String MESSAGE = ";MESSAGE=";
        private static final String BYE = "TYPE=BYE";
        private static final String farewell = "Bye! Come again soon!";

        private boolean connected = true;

        public MessageReceiver(DatagramSocket s, String t) {
            mSocket = s;
            token = t;
            mPacketBuffer = new byte[BUFFER_SIZE];
        }

        public void run() {
            Log.d(TAG, "Receiver activated.");
            while (connected) {
                try {
                    DatagramPacket mPacket = new DatagramPacket(mPacketBuffer, mPacketBuffer.length);
                    if (mSocket != null)
                    {
                        mSocket.receive(mPacket);
                        displayResults(mPacket);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void displayResults(DatagramPacket packet) {
            String received = new String(packet.getData(), 0, packet.getLength());
            if (received.startsWith(NEWMESSAGE)) {
                String username = token.substring(0, token.indexOf("|"));
                String senderName = received.substring(NEWMESSAGE.length(), received.indexOf(MESSAGE));
                String post = senderName + ": " + received.substring(received.indexOf(MESSAGE)+1);
                if (!username.equals(senderName)){
                    ((TextView)findViewById(R.id.textView)).setText(post);
                }
            }
            else if (received.startsWith(BYE)) {
                ((TextView)findViewById(R.id.textView)).setText(farewell);
                connected = false;
            }
        }
    }
}
