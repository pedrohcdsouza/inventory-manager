package com.pedrohcdsouza.server;

import com.pedrohcdsouza.server.service.ServerService;

public class Main {
    public static void main(String[] args) {
        ServerService server = new ServerService(8080);
        server.startServer();
    }
}
