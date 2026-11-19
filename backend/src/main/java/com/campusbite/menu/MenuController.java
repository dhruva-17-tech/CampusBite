package com.campusbite.menu;
import com.campusbite.users.*;import jakarta.validation.Valid;import org.springframework.http.HttpStatus;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;import java.util.List;
@RestController @RequestMapping("/api/menu")
public class MenuController {
 private final MenuService service;private final UserRepository users;
 public MenuController(MenuService service,UserRepository users){this.service=service;this.users=users;}
 @GetMapping public List<MenuItem> list(@RequestParam(required=false) String outlet){return service.list(outlet);}
 @GetMapping("/{id}") public MenuItem get(@PathVariable String id){return service.get(id);}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','OWNER')") public MenuItem create(@Valid @RequestBody MenuItemRequest r){return service.create(r);}
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','OWNER')") public MenuItem update(@PathVariable String id,@Valid @RequestBody MenuItemRequest r){return service.update(id,r);}
 private void checkOutlet(String id,Authentication a){AppUser u=users.findByEmail(a.getName()).orElseThrow();if(u.getRole()==Role.STAFF&&!service.get(id).getOutlet().equalsIgnoreCase(u.getAssignedOutlet()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Outside assigned outlet");}
 @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','OWNER')") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id){service.delete(id);}
 @PatchMapping("/{id}/stock") @PreAuthorize("hasAnyRole('STAFF','ADMIN','OWNER')") public MenuItem setStock(@PathVariable String id,@RequestParam int quantity,Authentication a){checkOutlet(id,a);return service.setStock(id,quantity);}
 @PatchMapping("/{id}/availability") @PreAuthorize("hasAnyRole('STAFF','ADMIN','OWNER')") public MenuItem setAvailability(@PathVariable String id,@RequestParam boolean available,Authentication a){checkOutlet(id,a);return service.setAvailability(id,available);}
}
