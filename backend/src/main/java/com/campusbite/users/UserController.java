package com.campusbite.users;
import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/users") public class UserController {
 private final UserRepository users; public UserController(UserRepository users){this.users=users;}
 @GetMapping("/me") public Map<String,Object> me(Authentication auth){AppUser u=users.findByEmail(auth.getName()).orElseThrow(); return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"role","ROLE_"+u.getRole().name(),"assignedOutlet",u.getAssignedOutlet()==null?"":u.getAssignedOutlet());}
}
