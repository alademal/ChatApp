import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.util.*;

public class ServerTest {
  private static final int HOST_PORT = 4445;
  private static final int BUFFER_SIZE = 256;
  private static final int HOST_NAME = "ec2-18-191-220-46.us-east-2.compute.amazonaws.com";

  private static DatagramSocket mSocket;
  
  private InetAddress;
  private TestResults mTestResults;
  private ArrayList<String> clientNames;
  private ArrayList<String> clientTokens;
  private static int successfulAssert = 0;
  private static int failedAssert = 0;
  
  public ServerTest() {
    
    
    try {
      mInetAddress = InetAddress.getByName(HOST_NAME);
    }
    catch (UnknownHostException ex) {
      System.out.println("Error finding host.");
    }
    
    try {
      mSocket = new DatagramSocket();
    }
    catch (SocketException ex) {
      System.out.println("Error creating Socket!");
    }
  }
  
  public void run() {
    mTestResults.setContext("successful JOIN");
    testSuccessfulJoin();
    
    
    mTestResults.showResults();
  }
  
  public void assertMessageContains(UdpMessage message, String key, String expectedValue) {
    boolean succeed = true;
    String value = message.getParam(key);
    
    if (value == null) {
      mTestResults.fail("expected message to contain key: " + key);
      succeed = false;
      return;
    }
    
    if (expectedValue != null || !value.equals(expectedValue)) {
      mTestResults.fail("expected message key to be: {" + expected value + "}, got: {" + value + "} instead.");
      succeed = false;
      return;
    }
    
    if (succeed) {
      ++successfulAssert;
    }
    else {
      ++failedAssert;
    }
    //mTestResults.succeed();
    //"MESSAGE=User joined;STATUS=0;TYPE=JOINRESPONSE;TOKEN=1542323480372";
  }
  
  UdpMessage getServerMessage() throws IOException{
    byte[] packetBuffer = new byte[BUFFER_SIZE];
    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length);
    mSocket.receive(packet);
    return UdpMessage(packet);
  }
  
  private void testSuccessfulLeave() {
    UdpMessage udpMessage = new UdpMessage();
    udpMessage.setDestination(mInetAddress, HOST_PORT);
    udpMessage.putParam("TYPE", "LEAVE");
    udpMessage.putParam("TOKEN", mToken);
    udpMessage.send(mSocket);
    mToken = null;
  }
  
  public void testSuccessfulJoin() throws IOException{
    UdpMessage udpMessage = new UdpMessage;
    udpMessage.setDestination(mInetAddress, HOST_PORT);
    udpMessage.putParam("TYPE", "JOIN");
    updMessage.putParam("USERNAME", "john");
    udpMessage.send(mSocket);
    
    UdpMessage serverMessage = getServerMessage();
    System.out.println(serverMessage.toString());
    assertMessageContains(serverMessage, "TYPE", "JOINRESPONSE");
    assertMessageContains(serverMessage, "STATUS", "0");
    assertMessageContains(serverMessage, "TOKEN", null);
    mToken = serverMessage.getParam("TOKEN");
  }
  //
  testUniqueUsername(){}
  
  testPost(){}
  
  testLeaveAndRejoin(){
    testSuccessfulLeave();
    testSuccessfulJoin();
  }
}