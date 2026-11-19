package com.campusbite.suppliers;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
@Document("suppliers")
public class Supplier {
 @Id private String id; private String name; private String contactName; private String phone; private String email; private String category; private String notes; private boolean active=true; private Instant createdAt=Instant.now();
 public Supplier() {}
 public Supplier(String name,String contactName,String phone,String email,String category,String notes){this.name=name;this.contactName=contactName;this.phone=phone;this.email=email;this.category=category;this.notes=notes;}
 public String getId(){return id;} public String getName(){return name;} public String getContactName(){return contactName;} public String getPhone(){return phone;} public String getEmail(){return email;} public String getCategory(){return category;} public String getNotes(){return notes;} public boolean isActive(){return active;} public Instant getCreatedAt(){return createdAt;} public void setActive(boolean v){active=v;}
}
