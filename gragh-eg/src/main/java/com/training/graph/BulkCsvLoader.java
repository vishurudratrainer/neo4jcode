package com.training.graph;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class BulkCsvLoader {
    public static void main(String[] args) {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "your_chosen_password";

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {

            // The 'file:///' prefix looks in the Neo4j 'import' directory
            String query =
                    "LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS row " +
                            "CALL { " +
                            "  WITH row " +
                            "  MERGE (p:Product {id: row.id}) " +
                            "  SET p.name = row.name, p.price = toFloat(row.price) " +
                            "} IN TRANSACTIONS OF 1000 ROWS";

            session.run(query).consume();
            System.out.println("Bulk CSV Insert Completed!");
        }
    }
}