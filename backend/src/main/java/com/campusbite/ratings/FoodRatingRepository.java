package com.campusbite.ratings;
import org.springframework.data.mongodb.repository.MongoRepository;import java.util.List;
public interface FoodRatingRepository extends MongoRepository<FoodRating,String>{boolean existsByOrderIdAndMenuItemIdAndCustomerEmail(String orderId,String menuItemId,String email);List<FoodRating> findByOutletOrderByCreatedAtDesc(String outlet);List<FoodRating> findByMenuItemId(String id);}
