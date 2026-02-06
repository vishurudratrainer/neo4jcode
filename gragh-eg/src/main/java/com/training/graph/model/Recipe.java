package com.training.graph.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Recipe")
public class Recipe {

    @Id // SDN uses this to match the 'P1' in findById
    private String id;
    private String recipeName;
    @Relationship(type = "HAS_INGREDIENT", direction = Relationship.Direction.OUTGOING)
    private List<RecipeIngredient> recipeIngredientList = new ArrayList<>();

    public void setId(String id) {
        this.id = id;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setRecipeIngredientList(List<RecipeIngredient> recipeIngredientList) {
        this.recipeIngredientList = recipeIngredientList;
    }

    public String getId() {
        return id;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public List<RecipeIngredient> getRecipeIngredientList() {
        return recipeIngredientList;
    }
}
