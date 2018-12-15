package ClientPackage;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.HashMap;
import java.util.Map;

public class UdpMessage {
  
  private static InetAddress mServerAddress;
  private static int port;
  private static byte[] buffer;
  private DatagramPacket mPacket;
  private String message = "";
  private HashMap<String, String> params = new HashMap<String, String>();
  
  public UdpMessage() {
  }
  
  public UdpMessage(DatagramPacket packet) {
    mPacket = packet;
    setDestination(packet.getAddress(), packet.getPort());
    fullMsg(new String(packet.getData(), 0, packet.getLength()));
    String[] arr = message.split(";");
    for (int i = 0; i < arr.length; ++i) {
        putParam(arr[i].substring(0, arr[i].indexOf("=")),
                arr[i].substring(arr[i].indexOf("=")+1));
    }
  }
  
  public UdpMessage(UdpMessage udpMessage) {
      this(udpMessage.mPacket);
      params = udpMessage.params;
  }
  
  public void setDestination(InetAddress serverAddr, int hostPort) {
    mServerAddress = serverAddr;
    port = hostPort;
  }
  
  public void fullMsg(String msg) {
    message = msg;
  }
  
  public void assignPacket(DatagramPacket packet) {
      mPacket = packet;
  }
  
  public void putParam(String key, String value) {
    params.put(key, value);
  }
  
  public String getParam(String key) {
    return params.get(key);
  }
  
  public void printMessage() {
      System.out.println(message);
  }
  
  public void send(DatagramSocket mSocket) throws IOException {
    buffer = message.getBytes();
    mPacket = new DatagramPacket(buffer, buffer.length, mServerAddress, port);
    mSocket.send(mPacket);
  }
}