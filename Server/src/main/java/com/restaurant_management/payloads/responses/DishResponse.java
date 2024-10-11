package com.restaurant_management.payloads.responses;

import com.restaurant_management.entites.Dish;
import com.restaurant_management.entites.DishImage;
import com.restaurant_management.entites.DishOptionSelection;
import com.restaurant_management.entites.Recipe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DishResponse {

    private String dishId;
    private String dishName;
    private String description;
    private String status;
    private String thumbImage;
    private Double offerPrice;
    private Double price;
    private String categoryId;
    private String categoryName;
    private List<DishImageResponse> images;
    private List<RecipeResponse> recipes;
    private List<ListOptionOfDishResponse> listOptions;

    public DishResponse(Dish dish, List<Recipe> recipes, List<DishImage> images) {
        this.dishId = dish.getId();
        this.dishName = dish.getDishName();
        this.description = dish.getDescription();
        this.status = dish.getStatus();
        this.thumbImage = dish.getThumbImage();
        this.offerPrice = dish.getOfferPrice();
        this.price = dish.getPrice();
        this.images = images.stream().map(DishImageResponse::new).toList();
        this.recipes = recipes.stream().map(RecipeResponse::new).toList();
        this.categoryId = dish.getCategory().getId();
        this.categoryName = dish.getCategory().getName();
    }

    public DishResponse(Dish dish, List<Recipe> recipes, List<DishImage> images, List<DishOptionSelection> optionSelections) {
        this.dishId = dish.getId();
        this.dishName = dish.getDishName();
        this.description = dish.getDescription();
        this.status = dish.getStatus();
        this.thumbImage = dish.getThumbImage();
        this.offerPrice = dish.getOfferPrice();
        this.price = dish.getPrice();
        this.images = images.stream().map(DishImageResponse::new).toList();
        this.recipes = recipes.stream().map(RecipeResponse::new).toList();
        this.categoryId = dish.getCategory().getId();
        this.categoryName = dish.getCategory().getName();

        this.listOptions = optionSelections.stream()
                .collect(Collectors.groupingBy(
                        selection -> selection.getDishOption().getOptionGroup(),
                        Collectors.mapping(
                                DishOptionSelectionResponse::new,
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(entry -> new ListOptionOfDishResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

}