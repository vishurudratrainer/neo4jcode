package com.training.graph.repo;

import com.training.graph.model.Product;
import com.training.graph.model.Recipe;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface RecipeRepository extends Neo4jRepository<Recipe, String> {
}
