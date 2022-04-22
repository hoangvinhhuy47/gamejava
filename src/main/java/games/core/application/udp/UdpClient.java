package games.core.application.udp;


import java.io.IOException;
import java.net.*;

/**
 * Created by tuanhoang on 10/14/17.
 */
public class UdpClient {
    public static void main(String args[]){

        byte[] buf = new byte[256];
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            System.out.println(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        buf= "hello".getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 25249);
        try {
            socket.send(packet);
            System.out.println(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
