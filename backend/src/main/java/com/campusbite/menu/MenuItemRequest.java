package com.campusbite.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuItemRequest(
                @NotBlank String name,
                String description,
                @NotBlank String category,
                @NotNull @DecimalMin("0.01") Double price,
                @NotNull @Min(0) Integer stock,
                @NotBlank String outlet,
                String imageUrl,
                boolean available) {
}
