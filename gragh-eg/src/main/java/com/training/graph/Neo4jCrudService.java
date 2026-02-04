package com.training.graph;

import org.neo4j.driver.*;
import java.util.*;

import org.neo4j.driver.*;
import java.util.*;

public class Neo4jCrudService implements AutoCloseable {
    private final Driver driver;

    public Neo4jCrudService(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    // 1. INSERT (MERGE) - Standard Managed Transaction
    public void upsertProduct(String id, String name, double price) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(
                    "MERGE (p:Product {id: $id}) " +
                            "SET p.name = $name, p.price = $price",
                    Map.of("id", id, "name", name, "price", price)
            ).consume());
        }
    }

    // 2. BATCH INSERT (Implicit Transaction)
    // FIX: Use session.run() directly for CALL ... IN TRANSACTIONS
    public void batchInsertProducts(List<Map<String, Object>> productList) {
        try (Session session = driver.session()) {
            // session.run() creates an "Auto-commit" or "Implicit" transaction
            session.run(
                    "UNWIND $batch AS row " +
                            "CALL { " +
                            "  WITH row " +
                            "  MERGE (p:Product {id: row.id}) " +
                            "  SET p.name = row.name, p.price = row.price " +
                            "} IN TRANSACTIONS OF 1000 ROWS",
                    Map.of("batch", productList)
            ).consume(); // consume() ensures all batches are processed by the server
        }
    }

    // 3. UPDATE (SET) - Standard Managed Transaction
    public void updateProductPrice(String id, double newPrice) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(
                    "MATCH (p:Product {id: $id}) SET p.price = $price",
                    Map.of("id", id, "price", newPrice)
            ).consume());
        }
    }

    // 4. DELETE (DETACH DELETE) - Standard Managed Transaction
    public void deleteProduct(String id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(
                    "MATCH (p:Product {id: $id}) DETACH DELETE p",
                    Map.of("id", id)
            ).consume());
        }
    }

    @Override
    public void close() {
        driver.close();
    }
}