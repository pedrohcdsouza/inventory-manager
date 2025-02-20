package com.pedrohcdsouza.server.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

import io.github.cdimascio.dotenv.Dotenv;



public class ServerSocketObj extends Thread {

    private static final int port = 8700;
    private Dotenv dotenv = Dotenv.load();
    private String authKey = dotenv.get("AUTH_KEY");
    private Socket client;
    private String outputResponse;

    public ServerSocketObj(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {
        try {

            client.setSoTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            String inputResponse = in.readLine();

            if (authKey == null) {
                System.out.println("[ERROR]: AUTH_KEY not founded.");
                client.close();
                return; 
            }

            if (!authKey.equals(inputResponse)) {
                System.out.println("[WARNING]: Client " + client.getInetAddress() + " Fail to Auth.");
                client.close();
                return; 
            }

            System.out.println("[AUTH]: Client " + client.getInetAddress() + " Authenticated.");

            outputResponse = "AUTH";
            out.println(outputResponse);

            

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startServer() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[INFO]: Waiting for clients ...");

            while (true) {
                Socket client = server.accept();
                System.out.println("[CONNECT]: Client " + client.getInetAddress() + " Connected.");

                ServerSocketObj svsock = new ServerSocketObj(client);
                svsock.start();
            }
        } catch (IOException e) {
            System.out.println("[ERROR]: " + e.getMessage());
        }
    }
}