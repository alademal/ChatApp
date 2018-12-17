import java.io.*;
import java.net.*;
import java.util.*;
//HW03
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ChatServer extends Thread {
  private static final int PORT = 4445;
  private static final int BUFFER_SIZE = 256;

  protected DatagramSocket mSocket;
  private ArrayList<String> usernames;
  private ArrayList<InetAddress> client_addresses;
  private ArrayList<Integer> client_ports;
  private String clientName;
  
  private static Map<String, String> existingClients;

  public ChatServer() throws IOException {
    this("ChatServer");
  }

  public ChatServer(String name) throws IOException {
    super(name);
    mSocket = new DatagramSocket(PORT);
    usernames = new ArrayList();
    client_addresses = new ArrayList();
    client_ports = new ArrayList();
    existingClients = new HashMap<String, String>();
  }

  public void run() {
    byte[] byteBuffer = new byte[BUFFER_SIZE];
    DatagramPacket clientPacket = new DatagramPacket(byteBuffer, byteBuffer.length);
    String prefix = "TYPE=";
    String responseString = "";

    while (true) {
      try {
        System.out.println("Listening for a client...");
        mSocket.receive(clientPacket);
        System.out.println("Got message from a client");
        String message = new String(clientPacket.getData(), 0, clientPacket.getLength());
        InetAddress clientAddress = clientPacket.getAddress();
        int clientPort = clientPacket.getPort();
        
        responseString = prefix;
        String[] parameters = message.split(";");
        if (parameters[0].startsWith(prefix) && (parameters[0].substring(parameters[0].indexOf('='))).equals("=JOIN")) {
          if (parameters[1].startsWith("USERNAME")) {
            clientName = parameters[1].substring(parameters[1].indexOf('=')+1);
            String token = clientName + "'s token";
            if (!usernames.contains(clientName)) {
              usernames.add(clientName);
              existingClients.put(token, clientName);
              client_ports.add(clientPort);
              client_addresses.add(clientAddress);
              responseString += "JOINRESPONSE;STATUS=0;TOKEN=" + token;
            }
            else if (!existingClients.containsValue(clientName)) {
              existingClients.put(token, clientName);
              client_ports.add(clientPort);
              client_addresses.add(clientAddress);
              responseString += "JOINRESPONSE;STATUS=0;TOKEN=" + token;
            }
            else {
              responseString += "JOINRESPONSE;STATUS=1;MESSAGE=error";
            }
            try {
              privateMsg(responseString, mSocket, clientAddress, clientPort);
            }
            catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        else if ((parameters[0].substring(parameters[0].indexOf('='))).equals("=POST")
                   && parameters[1].startsWith("TOKEN=")
                   && parameters[2].startsWith("MESSAGE=")) {
          String potentialToken = parameters[1].substring(parameters[1].indexOf('=')+1);
          String clientMessage = parameters[2].substring(parameters[2].indexOf('=')+1);
          
          if (existingClients.get(potentialToken) != null) {
            responseString += "NEWMESSAGE;USERNAME=" + existingClients.get(potentialToken) + ";MESSAGE=" + clientMessage;
            try {
              broadcast(responseString, mSocket);
            }
            catch (IOException e) {
              e.printStackTrace();
            }
          }
          else {
            System.out.println("Error: Can't post on chat server if never joined.");
          }
        }
        else if ((parameters[0].substring(parameters[0].indexOf('='))).equals("=LEAVE")
                   && parameters[1].startsWith("TOKEN=")) {
          String potentialToken = parameters[1].substring(parameters[1].indexOf('=')+1);
          if (existingClients.get(potentialToken) != null) {
            responseString += "BYE";
            existingClients.remove(potentialToken);
            client_ports.remove(Integer.valueOf(clientPort));
            client_addresses.remove(clientAddress);

            try {
              privateMsg(responseString, mSocket, clientAddress, clientPort);
            }
            catch (IOException e) {
              e.printStackTrace();
            }
          }
          else {
            System.out.println("Error: Can't leave chat server if never joined.");
          }
        }
        else {
          System.out.println("Error: Invalid Input.");
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    //mSocket.close();
  }
  
  private void broadcast(String message, DatagramSocket sock) throws IOException {
      byte[] buffer = message.getBytes();
      InetAddress clientAddr;
      int clientPrt;
      
      for (int i = 0; i < usernames.size(); ++i) {
        clientAddr = client_addresses.get(i);
        clientPrt = client_ports.get(i);
        DatagramPacket serverPacket = new DatagramPacket(buffer, buffer.length, clientAddr, clientPrt);
        
        try {
          sock.send(serverPacket);
        }
        catch (IOException e) {
          e.printStackTrace();
          System.out.println("Error: Failed to send post to client #" + i);
        }
      }
  }
  
  private void privateMsg(String message, DatagramSocket sock, InetAddress cAddr, int cPort) throws IOException {
    byte[] buffer = message.getBytes();
    DatagramPacket serverPacket = new DatagramPacket(buffer, buffer.length, cAddr, cPort);
    try {
      sock.send(serverPacket);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  
  public static void main(String args[]) throws Exception {
    ChatServer serverThread = new ChatServer();
    serverThread.start(); // Calls the 'run()' function
  }
}