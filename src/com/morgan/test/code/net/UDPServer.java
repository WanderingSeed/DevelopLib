package com.morgan.test.code.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP连接中的服务端。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-26
 */
class UDPServer {

    /**
     * 服务端启动后监听的端口
     */
    public static final int LISTEN_PORT = 5050;

    public static void main(String[] args) throws IOException {
        DatagramSocket server = new DatagramSocket(5050);
        byte[] recvBuf = new byte[100];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        server.receive(recvPacket);
        String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
        System.out.println("Received " + recvStr);

        int port = recvPacket.getPort();
        InetAddress addr = recvPacket.getAddress();
        String sendStr = "Hello! I'm server";
        byte[] sendBuf;
        sendBuf = sendStr.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, addr, port);
        server.send(sendPacket);
        server.close();
    }
}
