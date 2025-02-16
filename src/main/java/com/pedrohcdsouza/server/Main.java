package com.pedrohcdsouza.server;

import com.pedrohcdsouza.server.database.Database;

public class Main {
    Database db = new Database();
    db.addProduct("oi", 9, 8);
}