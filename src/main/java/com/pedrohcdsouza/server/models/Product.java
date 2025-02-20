package com.pedrohcdsouza.server.models;

public record Product(Integer id, String name, double price, int quantity) {
    public Product withName(String newName) {
        return new Product(this.id, newName, this.price, this.quantity);
    }
    
    public Product withPrice(double newPrice) {
        return new Product(this.id, this.name, newPrice, this.quantity);
    }
    
    public Product withQuantity(int newQuantity) {
        return new Product(this.id, this.name, this.price, newQuantity);
    }
}
