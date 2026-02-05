package com.training.graph;


import java.util.List;

public class App2 {
    public static void main(String[] args) {
        // Connect to local Neo4j instance
        try (GraphMasterclassTool tool = new GraphMasterclassTool("bolt://localhost:7687", "neo4j", "your_chosen_password")) {

            System.out.println("Starting Lab...");

            tool.setupSchema();
            tool.bulkInsert(List.of("Eve", "Frank","Alice"));
            tool.connectWithProps("Alice", "Eve", 2);
            tool.deleteOldLogs(30);
            tool.safeDeleteNode("E1"); // Alice

            System.out.println("Lab Completed Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
