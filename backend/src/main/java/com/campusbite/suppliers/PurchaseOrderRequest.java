package com.campusbite.suppliers;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.util.List;
public record PurchaseOrderRequest(@NotBlank String supplierId,@NotEmpty List<@Valid Line> items){public record Line(@NotBlank String itemName,@Min(1) int quantity,@DecimalMin("0.0") double unitCost){}}
