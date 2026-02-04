package com.training.graph;

import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        try (Neo4jCrudService db = new Neo4jCrudService("bolt://localhost:7687", "neo4j", "your_chosen_password")) {

            // Single Insert
            db.upsertProduct("P-001", "Gaming Mouse", 59.99);

            // Batch Insert
            List<Map<String, Object>> batch = List.of(
                    Map.of("id", "P-002", "name", "Keyboard", "price", 80.0),
                    Map.of("id", "P-003", "name", "Monitor", "price", 300.0)
            );
            db.batchInsertProducts(batch);

            // Update
            db.updateProductPrice("P-001", 49.99);

            // Delete
            db.deleteProduct("P-003");

            System.out.println("CRUD Operations Completed Successfully!");
        }
    }}
