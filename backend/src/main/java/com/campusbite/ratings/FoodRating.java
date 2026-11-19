package com.campusbite.ratings;
import org.springframework.data.annotation.Id;import org.springframework.data.mongodb.core.mapping.Document;import java.time.Instant;
@Document("food_ratings")
public class FoodRating {
 @Id private String id;private String orderId;private String menuItemId;private String itemName;private String customerEmail;private String outlet;private int rating;private String comment;private Instant createdAt=Instant.now();
 public FoodRating(){}public FoodRating(String oid,String mid,String name,String email,String outlet,int rating,String comment){orderId=oid;menuItemId=mid;itemName=name;customerEmail=email;this.outlet=outlet;this.rating=rating;this.comment=comment;}
 public String getId(){return id;}public String getOrderId(){return orderId;}public String getMenuItemId(){return menuItemId;}public String getItemName(){return itemName;}public String getCustomerEmail(){return customerEmail;}public String getOutlet(){return outlet;}public int getRating(){return rating;}public String getComment(){return comment;}public Instant getCreatedAt(){return createdAt;}
}
