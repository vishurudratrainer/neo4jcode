package com.training.graph;

import org.neo4j.driver.*;
import java.util.*;

public class GraphMasterclassTool implements AutoCloseable {
    private final Driver driver;

    public GraphMasterclassTool(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    // Schema Setup
    public void setupSchema() {
        try (Session session = driver.session()) {
            session.run("CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE");
        }
    }

    // Batch Insert (Unwind)
    public void bulkInsert(List<String> names) {
        driver.executableQuery("UNWIND $names AS name CREATE (:Person {name: name})")
                .withParameters(Map.of("names", names)).execute();
    }

    // Relationship Creation (Directed)
    public void connectWithProps(String p1, String p2, int years) {
        driver.executableQuery("MATCH (a:Person {name: $p1}), (b:Person {name: $p2}) " +
                        "MERGE (a)-[r:KNOWS {since_years: $years}]->(b)")
                .withParameters(Map.of("p1", p1, "p2", p2, "years", years)).execute();
    }

    // Conditional Delete
    public void deleteOldLogs(int days) {
        driver.executableQuery("MATCH (l:Log) WHERE l.ageDays > $days DETACH DELETE l")
                .withParameters(Map.of("days", days)).execute();
    }

    // Safe Delete (Always Detach)
    public void safeDeleteNode(String empId) {
        driver.executableQuery("MATCH (n:Employee {empId: $id}) DETACH DELETE n")
                .withParameters(Map.of("id", empId)).execute();
    }

    @Override
    public void close() { driver.close(); }
}