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
import com.pedrohcdsouza.server.utils.ServerLogger;

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
            ServerLogger.info("Server started on port " + port);
            new Thread(() -> acceptConnections()).start();
        } catch (IOException e) {
            ServerLogger.error("Failed to start server: " + e.getMessage());
        }
    }
    
    private void acceptConnections() {
        while (running) {
            try {
                ServerLogger.info("Waiting for client connections...");
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                ServerLogger.connect("New client connected from " + clientSocket.getInetAddress().getHostAddress());
                new Thread(clientHandler).start();
            } catch (IOException e) {
                if (running) {
                    ServerLogger.error("Connection acceptance failed: " + e.getMessage());
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
                ServerLogger.info("Server stopped successfully");
            }
        } catch (IOException e) {
            ServerLogger.error("Failed to stop server: " + e.getMessage());
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
                ServerLogger.auth("Client " + clientSocket.getInetAddress().getHostAddress() + " authenticated successfully");
                sendMessage("SUCCESS:Authentication successful");
            } else {
                ServerLogger.warning("Failed authentication attempt from " + clientSocket.getInetAddress().getHostAddress());
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
                        ServerLogger.info("Product added: " + newProduct);
                        sendMessage("SUCCESS");
                        break;
                        
                    case "REMOVE_PRODUCT":
                        int removeId = Integer.parseInt(command[1]);
                        database.removeProduct(removeId);
                        ServerLogger.info("Product removed: ID " + removeId);
                        sendMessage("SUCCESS");
                        break;
                        
                    case "UPDATE_PRODUCT":
                        Product updateProduct = new Product(Integer.parseInt(command[1]), command[2], Double.parseDouble(command[3]), Integer.parseInt(command[4]));
                        database.updateProduct(updateProduct);
                        ServerLogger.info("Product updated: " + updateProduct);
                        sendMessage("SUCCESS");
                        break;
                        
                    default:
                        ServerLogger.warning("Invalid command received: " + command[0]);
                        sendMessage("ERROR");
                        break;
                }
            } catch (NumberFormatException e) {
                ServerLogger.error("Invalid number format in command: " + e.getMessage());
                sendMessage("ERROR:Invalid number format");
            } catch (ArrayIndexOutOfBoundsException e) {
                ServerLogger.error("Missing parameters in command");
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
                if (clientSocket != null) {
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    clientSocket.close();
                    ServerLogger.connect("Client disconnected: " + clientAddress);
                }
                clients.remove(this);
            } catch (IOException e) {
                ServerLogger.error("Error closing client connection: " + e.getMessage());
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void broadcastMessage(String message) {
        clients.forEach(client -> client.sendMessage(message));
    }
}