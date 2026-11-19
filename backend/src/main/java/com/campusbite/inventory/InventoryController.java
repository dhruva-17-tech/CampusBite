package com.campusbite.inventory;
import com.campusbite.menu.*;import com.campusbite.users.*;import org.springframework.http.HttpStatus;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;import java.util.List;
@RestController @RequestMapping("/api/inventory") @PreAuthorize("hasAnyRole('STAFF','ADMIN','OWNER')")
public class InventoryController {
 private final MenuService menu;private final UserRepository users;
 public InventoryController(MenuService menu,UserRepository users){this.menu=menu;this.users=users;}
 private void checkOutlet(String id,Authentication auth){AppUser u=users.findByEmail(auth.getName()).orElseThrow();if(u.getRole()==Role.STAFF&&!menu.get(id).getOutlet().equalsIgnoreCase(u.getAssignedOutlet()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can manage inventory only for your assigned outlet");}
 @PatchMapping("/{id}/stock") public MenuItem stock(@PathVariable String id,@RequestParam int quantity,Authentication a){checkOutlet(id,a);return menu.setStock(id,quantity);}
 @PostMapping("/{id}/restock") public MenuItem restock(@PathVariable String id,@RequestParam int quantity,Authentication a){checkOutlet(id,a);return menu.addStock(id,quantity);}
 @PatchMapping("/{id}/availability") public MenuItem availability(@PathVariable String id,@RequestParam boolean available,Authentication a){checkOutlet(id,a);return menu.setAvailability(id,available);}
 @GetMapping("/low-stock") public List<MenuItem> lowStock(@RequestParam(defaultValue="5") int threshold,Authentication a){if(threshold<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Threshold cannot be negative");List<MenuItem> all=menu.lowStock(threshold);AppUser u=users.findByEmail(a.getName()).orElseThrow();return u.getRole()==Role.STAFF?all.stream().filter(x->x.getOutlet().equalsIgnoreCase(u.getAssignedOutlet())).toList():all;}
}
