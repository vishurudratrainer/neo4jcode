package com.training.graph;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import java.util.concurrent.*;
public class Neo4jConfig {

    Config config = Config.builder()
            .withMaxConnectionPoolSize(100)           // Default is 100
            .withConnectionTimeout(30, TimeUnit.SECONDS)
            .withMaxConnectionLifetime(1, TimeUnit.HOURS)
            .withConnectionAcquisitionTimeout(1, TimeUnit.MINUTES)
            .build();

    Driver driver = GraphDatabase.driver("bolt://localhost:7687",
            AuthTokens.basic("neo4j", "password"), config);
}
