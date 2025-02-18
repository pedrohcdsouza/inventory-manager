package com.pedrohcdsouza.server;

import com.pedrohcdsouza.server.database.Database;
import com.pedrohcdsouza.server.service.ServerSocketObj;

public class Main {
    public static void main(String[] args) {
        ServerSocketObj sv = new ServerSocketObj(null);
        sv.startServer();
    }
}
