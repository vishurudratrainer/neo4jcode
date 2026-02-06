package com.training.graph;

import com.training.graph.model.Ingredients;
import com.training.graph.model.Product;
import com.training.graph.model.Recipe;
import com.training.graph.model.RecipeIngredient;
import com.training.graph.repo.ProductRepository;
import com.training.graph.repo.RecipeRepository;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SdnApplication {
    @Bean
    public Driver neo4jDriver() {

        return GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "your_chosen_password")
        );
    }
    public static void main(String[] args) {
        SpringApplication.run(SdnApplication.class, args);
    }

    @Bean
    CommandLineRunner run(ProductRepository repo, RecipeRepository repository) {
        return args -> {
            // Clean start: Delete old data to avoid mapping ID conflicts
            repo.deleteAll();

            System.out.println("--- SDN CRUD START ---");

            // 1. Create
            Product p = new Product("P1", "Spring Boot Laptop", 1500.0);
            repo.save(p);

            // 2. Read
            repo.findById("P1").ifPresent(prod ->
                    System.out.println("Found: " + prod.getName()));

            System.out.println("--- SDN CRUD COMPLETE ---");
/**
            Recipe recipe =  new Recipe();
            recipe.setRecipeName("Vada pav");
            recipe.setId("1");
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setId(1L);
            ingredient.setQuantity(5);
            Ingredients ingredients = new Ingredients();
            ingredients.setIngName("Potato");
            ingredients.setId("1");
            ingredient.setIngredients(ingredients);
            recipe.getRecipeIngredientList().add(ingredient);
            repository.save(recipe);
**/
        };

    }
}