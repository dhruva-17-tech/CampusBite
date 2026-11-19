package com.campusbite.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String outlet,
        @NotBlank String paymentMethod,
        @NotEmpty List<@Valid ItemRequest> items,
        boolean useLoyaltyCoupon) {
    public record ItemRequest(@NotBlank String menuItemId, @NotNull @Min(1) Integer quantity) {
    }
}
