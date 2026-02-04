package com.training.graph;

import org.neo4j.driver.*;

public class NormalBatchDelete {
    public static void main(String[] args) {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "your_chosen_password";

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))) {
            try (Session session = driver.session()) {

                int batchSize = 5000;
                long totalDeleted = 0;
                boolean nodesLeft = true;

                System.out.println("Starting batch deletion...");

                while (nodesLeft) {
                    // This query finds a limited number of nodes and deletes them
                    long deletedInThisBatch = session.executeWrite(tx -> {
                        Result result = tx.run(
                                "MATCH (n:Product) " +
                                        "WITH n LIMIT $limit " +
                                        "DETACH DELETE n " +
                                        "RETURN count(*)",
                                Values.parameters("limit", batchSize)
                        );
                        return result.single().get(0).asLong();
                    });

                    totalDeleted += deletedInThisBatch;
                    System.out.println("Deleted " + totalDeleted + " nodes so far...");

                    // If we deleted fewer than the batch size, we are finished
                    if (deletedInThisBatch < batchSize) {
                        nodesLeft = false;
                    }
                }

                System.out.println("Cleanup complete. Total nodes removed: " + totalDeleted);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
