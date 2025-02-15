package server;

import server.service.ServerSocketObj;

public class Main {
    public static void main(String[] args) {
        ServerSocketObj server = new ServerSocketObj();
        server.startServer(9000);
    }
}