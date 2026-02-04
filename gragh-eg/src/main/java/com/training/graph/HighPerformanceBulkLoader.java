package com.training.graph;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class HighPerformanceBulkLoader {
    public static void main(String[] args) {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "your_chosen_password";

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {

            // 1. Create a Constraint first for speed (Crucial for millions of rows)
            session.run("CREATE CONSTRAINT product_id_idx IF NOT EXISTS FOR (p:Product) REQUIRE p.id IS UNIQUE");

            // 2. The High-Performance Batch Query
            String bulkQuery =
                    "LOAD CSV WITH HEADERS FROM 'file:///large_products.csv' AS row " +
                            "CALL { " +
                            "  WITH row " +
                            "  MERGE (p:Product {id: row.id}) " +
                            "  SET p.name = row.name, " +
                            "      p.price = toFloat(row.price), " +
                            "      p.category = row.category " +
                            "} IN TRANSACTIONS OF 5000 ROWS"; // Adjust batch size based on RAM

            System.out.println("Starting import of millions of rows...");

            // 3. Use session.run() (Implicit) because of IN TRANSACTIONS
            session.run(bulkQuery).consume();

            System.out.println("Import Successful!");
        }
    }
}