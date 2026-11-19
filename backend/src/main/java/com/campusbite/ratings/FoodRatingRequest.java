package com.campusbite.ratings;
import jakarta.validation.constraints.*;
public record FoodRatingRequest(@NotBlank String orderId,@NotBlank String menuItemId,@Min(1) @Max(5) int rating,@Size(max=500) String comment){}
