package com.pedrohcdsouza.server;

import com.pedrohcdsouza.server.database.Database;

public class Main {
    public static void main(String[] args) {
        Database banco = new Database();
        banco.adicionar(0, "pedro", 0.0, 0);
    }
}
