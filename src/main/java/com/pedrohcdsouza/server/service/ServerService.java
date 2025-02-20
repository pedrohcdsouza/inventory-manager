package com.pedrohcdsouza.server.service;

import java.io.*;
import java.net.*;
import java.util.*;
import io.github.cdimascio.dotenv.Dotenv;
import com.pedrohcdsouza.server.database.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.pedrohcdsouza.server.models.Product;

public class ServerService {
    private ServerSocket serverSocket;
    private boolean running;
    private final int port;
    private List<ClientHandler> clients;
    private Database database;
    
    public ServerService(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        try {
            String dbPath = "jdbc:sqlite::resource:stock.db";
            Connection connection = DriverManager.getConnection(dbPath);
            this.database = new Database(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
    
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server started on port " + port);
            new Thread(() -> acceptConnections()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void acceptConnections() {
        while (running) {
            try {
                System.out.println("Waiting for client...");
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void stopServer() {
        running = false;
        clients.forEach(ClientHandler::closeConnection);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;
        private boolean isAuthenticated = false;
        private final String AUTH_KEY;
        
        public ClientHandler(Socket socket) {
            Dotenv dotenv = Dotenv.load();
            AUTH_KEY = dotenv.get("AUTH_KEY");
            this.clientSocket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            try {
                String message;
                if ((message = input.readLine()) != null) {
                    String[] command = message.split(":");
                    if (command[0].equals("AUTH") && command.length == 2) {
                        handleAuth(command[1]);
                    } else {
                        sendMessage("ERROR:First message must be authentication");
                        closeConnection();
                        return;
                    }
                }
                
                while (isAuthenticated && (message = input.readLine()) != null) {
                    String[] command = message.split(":");
                    handleCommand(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }
        
        private void handleAuth(String key) {
            if (AUTH_KEY != null && AUTH_KEY.equals(key)) {
                this.isAuthenticated = true;
                sendMessage("SUCCESS:Authentication successful");
            } else {
                sendMessage("ERROR:Invalid authentication key");
                closeConnection();
            }
        }
        
        private void handleCommand(String[] command) {
            try {
                switch (command[0]) {
                    case "ADD_PRODUCT":
                        Product newProduct = new Product(null, command[1], Double.parseDouble(command[2]), Integer.parseInt(command[3]));
                        database.addProduct(newProduct);
                        sendMessage("SUCCESS");
                        break;
                        
                    case "REMOVE_PRODUCT":
                        database.removeProduct(Integer.parseInt(command[1]));
                        sendMessage("SUCCESS");
                        break;
                        
                    case "UPDATE_PRODUCT":
                        Product updateProduct = new Product(Integer.parseInt(command[1]), command[2], Double.parseDouble(command[3]), Integer.parseInt(command[4]));
                        database.updateProduct(updateProduct);
                        sendMessage("SUCCESS");
                        break;
                        
                    default:
                        sendMessage("ERROR");
                        break;
                }
            } catch (NumberFormatException e) {
                sendMessage("ERROR:Invalid number format");
            } catch (ArrayIndexOutOfBoundsException e) {
                sendMessage("ERROR:Missing parameters");
            }
        }
        
        public void sendMessage(String message) {
            output.println(message);
        }
        
        public void closeConnection() {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (clientSocket != null) clientSocket.close();
                clients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void broadcastMessage(String message) {
        clients.forEach(client -> client.sendMessage(message));
    }
}