package com.morgan.test.code.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Socket连接中的服务端。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-26
 */
public class SocketServer {

    /**
     * 服务端启动后监听的端口
     */
    public static final int LISTEN_PORT = 3535;
    private boolean mRun = false;
    private ServerSocket sever;
    private List<Socket> clients = new ArrayList<Socket>();

    public SocketServer(int port) {
        try {
            sever = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动服务端
     */
    public void startService() {
        mRun = true;
        new Thread() {

            public void run() {
                while (mRun) {
                    try {
                        final Socket socket = sever.accept();
                        clients.add(socket);
                        new Thread(new Runnable() {

                            public void run() {
                                BufferedReader in;
                                try {
                                    System.out.println("Thread " + Thread.currentThread().getId() + " is start");
                                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                                    while (!socket.isClosed()) {
                                        String str;
                                        str = in.readLine();
                                        System.out.println("Thread " + Thread.currentThread().getId() + "Received: "
                                                + str);
                                        if (str == null || str.equals("end")) {
                                            out.println("End talk");
                                            out.flush();
                                            System.out.println("Thread " + Thread.currentThread().getId() + " is end");
                                            clients.remove(socket);
                                            break;
                                        }
                                        out.println("Hello, I'm server");
                                        out.flush();
                                    }
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (IOException e) {
                        if (mRun) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }.start();
        System.out.println("Server is started");
    }

    /**
     * 关闭服务端
     */
    public void stopService() {
        try {
            mRun = false;
            for (int i = clients.size() - 1; i >= 0; i--) {
                clients.get(i).close();
            }
            clients.clear();
            sever.close();
            System.out.println("Server is stoped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketServer server = new SocketServer(LISTEN_PORT);
        server.startService();

        try {
            Thread.sleep(15000);
        } catch (Exception e) {
        }

        server.stopService();
    }
}