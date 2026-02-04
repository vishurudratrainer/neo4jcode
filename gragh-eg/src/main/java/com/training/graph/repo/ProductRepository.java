package com.training.graph.repo;
import com.training.graph.model.Product;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import java.util.List;

public interface ProductRepository extends Neo4jRepository<Product, String> {

    // Works automatically - Spring writes: MATCH (n:Product) WHERE n.name CONTAINS $0 RETURN n
    List<Product> findByNameContaining(String name);

    // If this is just an update, return the count of affected rows (Integer/Long)
    @Query("MATCH (p:Product) WHERE p.price > $threshold SET p.onSale = true RETURN count(p)")
    Long markExpensiveProductsAsOnSale(Double threshold);
}