package com.campusbite.requests;
import jakarta.validation.constraints.*;
public record CookingRequestPayload(@NotBlank String orderId,@NotBlank @Size(max=500) String requestText){}
