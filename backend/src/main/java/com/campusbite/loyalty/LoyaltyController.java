package com.campusbite.loyalty;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loyalty")
@PreAuthorize("hasRole('CUSTOMER')")
public class LoyaltyController {
    private final LoyaltyService service;

    public LoyaltyController(LoyaltyService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public LoyaltySummary myLoyalty(Authentication authentication) {
        return service.summary(authentication.getName());
    }
}
