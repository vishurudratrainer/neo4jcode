package com.training.graph.service;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;
import org.neo4j.driver.reactive.ReactiveSession;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class ReactiveProductService {
    private final Driver driver;

    public ReactiveProductService(Driver driver) {
        this.driver = driver;
    }

    // 1. CREATE
    public Mono<Void> createProduct(String id, String name) {
        return Mono.usingWhen(
                Mono.fromSupplier(() -> driver.session(ReactiveSession.class)),
                session -> JdkFlowAdapter.flowPublisherToFlux(
                        session.executeWrite(tx ->
                                JdkFlowAdapter.publisherToFlowPublisher(
                                        JdkFlowAdapter.flowPublisherToFlux(tx.run(
                                                "CREATE (p:Product {id: $id, name: $name})",
                                                java.util.Map.of("id", id, "name", name)
                                        )).then()
                                )
                        )
                ).then(),
                session -> JdkFlowAdapter.flowPublisherToFlux(session.close())
        );
    }

    // 2. READ (Streaming Names)
    public Flux<String> streamAllProductNames() {
        return Flux.usingWhen(
                Mono.fromSupplier(() -> driver.session(ReactiveSession.class)),
                session -> JdkFlowAdapter.flowPublisherToFlux(
                        session.executeRead(tx ->
                                JdkFlowAdapter.publisherToFlowPublisher(
                                        JdkFlowAdapter.flowPublisherToFlux(tx.run("MATCH (p:Product) RETURN p.name AS name"))
                                                .flatMap(result -> JdkFlowAdapter.flowPublisherToFlux(result.records()))
                                                .map(record -> record.get("name").asString())
                                )
                        )
                ),
                session -> JdkFlowAdapter.flowPublisherToFlux(session.close())
        );
    }

    public Mono<Void> deleteAllProducts() {
        return Mono.usingWhen(
                Mono.fromSupplier(() -> driver.session(ReactiveSession.class)),
                session -> JdkFlowAdapter.flowPublisherToFlux(
                        session.executeWrite(tx ->
                                JdkFlowAdapter.publisherToFlowPublisher(
                                        JdkFlowAdapter.flowPublisherToFlux(tx.run(
                                                "MATCH (p:Product) " +
                                                        "CALL { " +
                                                        "  WITH p " +
                                                        "  DETACH DELETE p " +
                                                        "} IN TRANSACTIONS OF 5000 ROWS"
                                        )).then()
                                )
                        )
                ).then(),
                session -> JdkFlowAdapter.flowPublisherToFlux(session.close())
        );
    }
}