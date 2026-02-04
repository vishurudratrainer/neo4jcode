package com.training.graph.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Product") // Ensure this is exactly "Product" (case-sensitive)
public class Product {

    @Id // SDN uses this to match the 'P1' in findById
    private String id;

    private String name;

    private Double price;

    // 1. MANDATORY: No-args constructor for the Reflection mapper
    public Product() {}

    // 2. Full constructor for your logic
    public Product(String id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // 3. Getters and Setters (Required for mapping)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}