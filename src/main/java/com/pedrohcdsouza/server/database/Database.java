package com.pedrohcdsouza.server.database;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.*;

public class Database {

    private static final String FILE_PATH = "src/main/resources/stock.csv";

    public void addProduct(String name, double price, int quantity) throws IOException{
        List<String[]> products = Read();

        for(String[] product : products){
            if(product[1].equals(name)){
                System.out.println("Error: product alreads exist.");
                return;
            }
            int id = products.size() + 1;
            String[] newProduct = {String.valueOf(id),name,String.valueOf(price), String.valueOf(quantity)};
            products.add(newProduct);
            Write(products);
            System.out.println("Product added: " + name);
        }
    }

    private List<String[]> Read() throws IOException {
        List<String[]> products = new ArrayList<>();
        
        // Criando um CSVReader para ler o arquivo
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            String[] line;
            try {
                while ((line = reader.readNext()) != null) {
                    products.add(line);
                }
            } catch (CsvValidationException e) {
                System.out.println("Erro de validação CSV: " + e.getMessage());
            }
        }

        return products;
    }

    private void Write(List<String[]> products) throws IOException{
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))){
            writer.writeAll(products);
        }
    }
}