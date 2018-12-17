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
    private static byte[] buffer;

    private static final String POST = "TYPE=POST;TOKEN=";
    private static final String MESSAGE = ";MESSAGE=";
    private static final String LEAVE = "TYPE=LEAVE;TOKEN=";
    private static final String JOINMESSAGE = "TYPE=JOIN;USERNAME=";
    private static final String JOINRESPONSE_0 = "TYPE=JOINRESPONSE;STATUS=0;TOKEN=";
    private static final String NEWMESSAGE = "TYPE=NEWMESSAGE;USERNAME=";
    private static final String BYE = "TYPE=BYE";
    private static final String farewell = "Bye! Come again soon!";
    private static String token = "";
    private static String message = "";
    private static final String TAG = "HW03";
    private boolean readyToSend = false;
    private boolean connected1 = true;
    private boolean connected2 = true;
    private boolean firstMsg = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String info1 = "Welcome to HW03Chat! What is your name?";
        final String info2 = "Successfully joined HW03Chat!\nEnter 'LEAVE' to exit.";

        try {
            mSocket = new DatagramSocket();
        }
        catch (SocketException e) {
            e.printStackTrace();
        }

        Thread messageSender = new Thread(new Runnable() {
            DatagramPacket packet;

            private String encodeMessage(String message) throws IOException {
                String encodedMsg = "";
                if (firstMsg) {
                    encodedMsg = JOINMESSAGE + message;
                    firstMsg = false;
                } else if (message.equals("LEAVE")) {
                    encodedMsg = LEAVE + token;
                    connected1 = false;
                    firstMsg = true;
                } else {
                    encodedMsg = POST + token + MESSAGE + message;
                    Log.d(TAG, "Sent: " + encodedMsg);
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
                ((TextView)findViewById(R.id.textView)).setText(info1);
                while (connected1) {
                    while (!readyToSend) {
                    }
                    message = ((EditText) findViewById(R.id.editText)).getText().toString();
                    try {
                        String encodedMessage = encodeMessage(message);
                        deliverMessage(encodedMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    readyToSend = false;
                }
            }
        });

        Thread messageReceiver = new Thread(new Runnable() {
            public void run() {
                Log.d(TAG, "Receiver activated.");
                while (connected2) {
                    try {
                        buffer = new byte[BUFFER_SIZE];
                        DatagramPacket mPacket = new DatagramPacket(buffer, buffer.length);
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
                if (received.startsWith(JOINRESPONSE_0)) {
                    token = received.substring(JOINRESPONSE_0.length());
                    ((TextView)findViewById(R.id.textView)).setText(info2);
                }
                if (received.startsWith(NEWMESSAGE)) {
                    String username = token.substring(0, token.indexOf("|"));
                    String senderName = received.substring(NEWMESSAGE.length(), received.indexOf(MESSAGE));
                    String post = senderName + ": " + received.substring(received.indexOf(MESSAGE)+1);
                    if (!username.equals(senderName)){
                        ((TextView)findViewById(R.id.textView)).setText(post);
                    }
                    else {
                        ((TextView)findViewById(R.id.textView)).setText("");
                    }
                }
                else if (received.startsWith(BYE)) {
                    ((TextView)findViewById(R.id.textView)).setText(farewell);
                    connected2 = false;
                }
                else {
                    String error = "ERROR";
                    ((TextView)findViewById(R.id.textView)).setText(error);
                }
            }
        });

        messageSender.start();
        messageReceiver.start();
    }

    public void sendMsg(View view) {
        readyToSend = true;
    }
}
