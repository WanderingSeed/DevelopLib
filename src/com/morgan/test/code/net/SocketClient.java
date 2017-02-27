package com.morgan.test.code.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Socket连接中的客户端。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-26
 */
public class SocketClient {

    private Socket client;

    public SocketClient(String site, int port) {
        try {
            client = new Socket(site, port);
            System.out.println("Client is created! site:" + site + " port:" + port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息，如果发送end则结束通讯
     * 
     * @param msg
     * @return
     */
    public String sendMsg(String msg) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(msg);
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void closeSocket() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        SocketClient client = new SocketClient("127.0.0.1", SocketServer.LISTEN_PORT);
        System.out.println("Received: " + client.sendMsg("Hello, I'm client"));
        System.out.println("Received: " + client.sendMsg("end"));
    }
}