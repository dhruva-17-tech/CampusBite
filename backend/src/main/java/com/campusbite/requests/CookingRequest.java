package com.campusbite.requests;
import org.springframework.data.annotation.Id;import org.springframework.data.mongodb.core.mapping.Document;import java.time.Instant;
@Document("cooking_requests")
public class CookingRequest {
 @Id private String id;private String orderId;private String customerEmail;private String outlet;private String requestText;private String status="SUBMITTED";private String staffNote="";private Instant createdAt=Instant.now();private Instant updatedAt=createdAt;
 public CookingRequest(){} public CookingRequest(String oid,String email,String outlet,String text){orderId=oid;customerEmail=email;this.outlet=outlet;requestText=text;}
 public String getId(){return id;}public String getOrderId(){return orderId;}public String getCustomerEmail(){return customerEmail;}public String getOutlet(){return outlet;}public String getRequestText(){return requestText;}public String getStatus(){return status;}public String getStaffNote(){return staffNote;}public Instant getCreatedAt(){return createdAt;}public Instant getUpdatedAt(){return updatedAt;}public void update(String s,String n){status=s;staffNote=n==null?"":n;updatedAt=Instant.now();}
}
