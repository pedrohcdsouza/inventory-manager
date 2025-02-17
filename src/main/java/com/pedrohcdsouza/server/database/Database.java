package com.pedrohcdsouza.server.database;

import java.sql.*;
import java.util.*;

public class Database {
    private static final String URL = "jdbc:sqlite:src/main/resources/stock.db";

    public Database() {
        criarTabela();
    }

    private void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS produtos (" +
                     "id INTEGER PRIMARY KEY, " +
                     "nome TEXT NOT NULL, " +
                     "preco REAL NOT NULL, " +
                     "quantidade INTEGER NOT NULL)";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public List<String[]> lerTodos() {
        List<String[]> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produtos.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nome"),
                    String.valueOf(rs.getDouble("preco")),
                    String.valueOf(rs.getInt("quantidade"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao ler produtos: " + e.getMessage());
        }
        return produtos;
    }

    public void adicionar(int id, String nome, double preco, int quantidade) {
        String sql = "INSERT INTO produtos (id, nome, preco, quantidade) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, nome);
            pstmt.setDouble(3, preco);
            pstmt.setInt(4, quantidade);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    public void atualizar(int id, String nome, double preco, int quantidade) {
        String sql = "UPDATE produtos SET nome = ?, preco = ?, quantidade = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setDouble(2, preco);
            pstmt.setInt(3, quantidade);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    public void remover(int id) {
        String sql = "DELETE FROM produtos WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao remover produto: " + e.getMessage());
        }
    }
}
