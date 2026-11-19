package com.campusbite.users;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
@Document(collection="users")
public class AppUser {
 @Id private String id; private String name;
 @Indexed(unique=true) private String email;
 private String password; private Role role; private String assignedOutlet;
 private boolean enabled=true; private Instant createdAt;
 public AppUser() {}
 public AppUser(String name,String email,String password,Role role){this(name,email,password,role,null);}
 public AppUser(String name,String email,String password,Role role,String assignedOutlet){this.name=name;this.email=email;this.password=password;this.role=role;this.assignedOutlet=assignedOutlet;this.createdAt=Instant.now();this.enabled=true;}
 public String getId(){return id;} public String getName(){return name;} public String getEmail(){return email;} public String getPassword(){return password;} public Role getRole(){return role;} public String getAssignedOutlet(){return assignedOutlet;} public boolean isEnabled(){return enabled;} public Instant getCreatedAt(){return createdAt;}
 public void setRole(Role role){this.role=role;} public void setAssignedOutlet(String outlet){this.assignedOutlet=outlet;} public void setEnabled(boolean enabled){this.enabled=enabled;}
}
