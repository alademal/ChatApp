import UdpMsg.*;

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.util.*;
import java.net.Socket;

public class ServerTest {

    private static final int SERVER_PORT = 4445;
    private static final int BUFFER_SIZE = 256;
    private static final String HOST_NAME = "localhost";

    private static DatagramSocket mSocket;

    private static InetAddress mServerAddress;
    private static int successfulAssert = 0;
    private static int failedAssert = 0;
    private static int untested = 0;
    private static final String username = "alade";
    private static String mToken;
    private static UdpMessage serverMessage = new UdpMessage();
    private static ArrayList<ArrayList<String>> allTests = new ArrayList<ArrayList<String>>();
    private static final String[] allTestsHeaders = {"successful JOIN: ",
        "successful LEAVE: ", "successful JOIN and REJOIN: ",
        "fail to double JOIN: ", "successful POST: "};

    public static void main(String[] args) throws IOException {
        System.out.println("0");
        initialTest();
        System.out.println("1");
        try {
            testSuccessfulJoin();
        }
        catch (IOException e) {
            ++untested;
        }
        System.out.println("2");
        
        try {
            testSuccessfulLeave();
        }
        catch (IOException e) {
            ++untested;
        }
        System.out.println("3");
        try {
            testLeaveAndRejoin();
        }
        catch (IOException e) {
            ++untested;
        }
        System.out.println("4");
        try {
            testUniqueUsername();
        }
        catch (IOException e) {
            ++untested;
        }
        System.out.println("5");
        try {
            testPost();
        }
        catch (IOException e) {
            ++untested;
        }
        
        System.out.println("Test results\n"
                + "------------\n"
                + "\n"
                + "Successful assertions: " + successfulAssert + "\n"
                + "\n"
                + "Failed assertions: " + failedAssert + "\n"
                + "\n"
                + "Untested assertions: " + untested + "\n");

        for (int i = 0; i < allTests.size(); ++i) {
            for (int j = 0; j < (allTests.get(i)).size(); ++j) {
                System.out.print(allTestsHeaders[i]);
                System.out.println((allTests.get(i)).get(j));
            }
        }
    }
    
    

    public static void assertMessageContains(ArrayList<String> array, UdpMessage message, String key, String expectedValue) {
        String value = message.getParam(key);

        if (value == null) {
            ++failedAssert;
            array.add("expected message to contain key: " + key);
            return;
        }
        
        if (!value.equals(expectedValue)) {
            ++failedAssert;
            array.add("expected message key to be: {" + expectedValue + "}, got: {" + value + "} instead.");
            return;
        }

        ++successfulAssert;
    }
    
    public static void initialTest() {

        try {
            mServerAddress = InetAddress.getByName(HOST_NAME);
        } catch (UnknownHostException ex) {
            System.out.println("Error finding host.");
        }

        try {
            mSocket = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println("Error creating Socket!");
        }
    }
    
    private static void getServerMessage() {
        
        boolean msgReceived = false;
        String gotMsg = "";
        
        serverMessage = new UdpMessage();
            byte[] packetBuffer = new byte[BUFFER_SIZE];    
            DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length,
                mServerAddress, SERVER_PORT);
            while (!msgReceived) {
                try {
                    mSocket.receive(packet);
                    gotMsg = new String(packet.getData(), 0, packet.getLength());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                if (!gotMsg.equals("")) {
                    serverMessage.fullMsg(gotMsg);
                    String[] parameters = gotMsg.split(";");
                    for (int i = 0; i < parameters.length; ++i) {
                        System.out.println(parameters[i]);
                        String key = parameters[i].substring(0, parameters[i].indexOf("="));
                        String value = parameters[i].substring(parameters[i].indexOf("=")+1);
                        serverMessage.putParam(key, value);
                    }
                    msgReceived = true;
                }
            }
            serverMessage.assignPacket(packet);
    }

    public static void join() throws IOException {
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setDestination(mServerAddress, SERVER_PORT);
        udpMessage.putParam("TYPE", "JOIN");
        udpMessage.putParam("USERNAME", username);
        udpMessage.fullMsg("TYPE=JOIN;USERNAME=" + username);
        udpMessage.send(mSocket);
    }

    public static void post(String message) throws IOException {
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setDestination(mServerAddress, SERVER_PORT);
        udpMessage.putParam("TYPE", "POST");
        udpMessage.putParam("TOKEN", mToken);
        udpMessage.putParam("MESSAGE", message);
        udpMessage.fullMsg("TYPE=POST;TOKEN=" + mToken + ";MESSAGE=" + message);
        udpMessage.send(mSocket);
    }
    
    public static void leave() throws IOException {
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setDestination(mServerAddress, SERVER_PORT);
        udpMessage.putParam("TYPE", "LEAVE");
        udpMessage.putParam("TOKEN", mToken);
        udpMessage.fullMsg("TYPE=LEAVE;TOKEN=" + mToken);
        udpMessage.send(mSocket);
    }

    public static void testSuccessfulJoin() throws IOException {
        join();
        System.out.println("1.5");
        getServerMessage();
        serverMessage.printMessage();
        System.out.println("1.6");
        ArrayList<String> arr = new ArrayList<String>();
        assertMessageContains(arr, serverMessage, "TYPE", "JOINRESPONSE");
        assertMessageContains(arr, serverMessage, "STATUS", "0");
        String expectedToken = username + "'s token"; 
        assertMessageContains(arr, serverMessage, "TOKEN", expectedToken);
        allTests.add(arr);
        System.out.println("1.7");
        mToken = serverMessage.getParam("TOKEN");
    }
    
    private static void testSuccessfulLeave() throws IOException {
        leave();

        getServerMessage();
        ArrayList<String> arr = new ArrayList<String>();
        assertMessageContains(arr, serverMessage, "TYPE", "BYE");
        allTests.add(arr);
    }
    
    public static void testLeaveAndRejoin() throws IOException {
        testSuccessfulJoin();
    }

    public static void testUniqueUsername() throws IOException {
        join();
        getServerMessage();
        join();
        getServerMessage();

        serverMessage.printMessage();
        ArrayList<String> arr = new ArrayList<String>();
        assertMessageContains(arr, serverMessage, "TYPE", "JOINRESPONSE");
        assertMessageContains(arr, serverMessage, "STATUS", "1");
        assertMessageContains(arr, serverMessage, "MESSAGE", "error");
        allTests.add(arr);
    }

    public static void testPost() throws IOException {
        String message = "test post";
        post(message);
        getServerMessage();
        
        serverMessage.printMessage();
        ArrayList<String> arr = new ArrayList<String>();
        assertMessageContains(arr, serverMessage, "TYPE", "NEWMESSAGE");
        assertMessageContains(arr, serverMessage, "USERNAME", username);
        assertMessageContains(arr, serverMessage, "MESSAGE", message);
        allTests.add(arr);
        
        leave();
    }
}
