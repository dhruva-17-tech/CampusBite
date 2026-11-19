package com.campusbite.loyalty;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface LoyaltyAccountRepository extends MongoRepository<LoyaltyAccount, String> {
    Optional<LoyaltyAccount> findByCustomerEmailIgnoreCase(String customerEmail);
}
