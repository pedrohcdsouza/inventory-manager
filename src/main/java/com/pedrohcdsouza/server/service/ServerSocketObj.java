package com.pedrohcdsouza.server.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketObj {
    public void startServer(int port) {
        System.out.println("Starting server ...");
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            while (true) {
                Socket client = server.accept();
                System.out.println("Client " + client.getInetAddress().getHostAddress() + " connected.");

                client.close();
                System.out.println("Client " + client.getInetAddress().getHostAddress() + " desconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}