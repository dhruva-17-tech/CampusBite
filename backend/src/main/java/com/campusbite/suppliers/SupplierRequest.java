package com.campusbite.suppliers;
import jakarta.validation.constraints.*;
public record SupplierRequest(@NotBlank String name,String contactName,String phone,@Email String email,String category,String notes) {}
