package com.training.graph;

import com.training.graph.service.ReactiveProductService;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class ReactiveMain {
    public static void main(String[] args) {
        // 1. Setup Driver
        Driver driver = GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic("neo4j", "your_chosen_password"));

        ReactiveProductService service = new ReactiveProductService(driver);

        System.out.println("--- REACTIVE CRUD START ---");

        // 1. Create (We use .block() here to wait for completion in a simple Main method)
        service.createProduct("RP-99", "Reactive Controller")
                .doOnSuccess(v -> System.out.println("Product Created!"))
                .block();

        // 2. Read (Stream all names)
        service.streamAllProductNames()
                .doOnNext(name -> System.out.println("Streaming Product: " + name))
                .collectList() // Collect all into a list
                .block();

     /**   // 3. Update
        service.updatePrice("RP-99", 299.99)
                .doOnNext(count -> System.out.println("Updated " + count + " records."))
                .block();

        // 4. Delete
        service.deleteProduct("RP-99")
                .doOnSuccess(v -> System.out.println("Product Deleted!"))
                .block();
**/
        System.out.println("--- REACTIVE CRUD COMPLETE ---");
        driver.close();
    }
}