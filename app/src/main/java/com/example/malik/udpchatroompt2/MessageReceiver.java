package com.example.malik.udpchatroompt2;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class MessageReceiver implements Runnable{
  private static final int BUFFER_SIZE = 256;
  private static DatagramSocket mSocket;
  private static byte[] mPacketBuffer;
  private static String token;
  
  private static final String NEWMESSAGE = "TYPE=NEWMESSAGE;USERNAME=";
  private static final String MESSAGE = ";MESSAGE=";
  private static final String BYE = "TYPE=BYE";
  
  private static boolean connected = true;
  
  public MessageReceiver(DatagramSocket s, String t) {
    mSocket = s;
    token = t;
    mPacketBuffer = new byte[BUFFER_SIZE];
  }
  
  public void run() {
    System.out.println("Receiver activated.");
    while (connected) {
      try {
        DatagramPacket mPacket = new DatagramPacket(mPacketBuffer, mPacketBuffer.length);
        mSocket.receive(mPacket);
        displayResults(mPacket);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    //return packet;
  }

  private static void displayResults(DatagramPacket packet) {
    String received = new String(packet.getData(), 0, packet.getLength());
    if (received.startsWith(NEWMESSAGE)) {
      String username = token.substring(0, token.indexOf("|"));
      String senderName = received.substring(NEWMESSAGE.length(), received.indexOf(MESSAGE));
      System.out.println("Compare - Username: " + username + " and Sendername: " + senderName);
      if (!username.equals(senderName)){
        System.out.println(senderName + ": " + received.substring(received.indexOf(MESSAGE)+1));
      }
    }
    else if (received.startsWith(BYE)) {
      System.out.println("Bye! Come again soon!");
      connected = false;
    }
  }
}