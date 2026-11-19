package com.campusbite.suppliers;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/suppliers") @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
public class SupplierController {
 private final SupplierRepository repo;
 public SupplierController(SupplierRepository r){repo=r;}
 @GetMapping public List<Supplier> all(){return repo.findAll();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Supplier create(@Valid @RequestBody SupplierRequest r){return repo.save(new Supplier(r.name().trim(),r.contactName(),r.phone(),r.email(),r.category(),r.notes()));}
 @PutMapping("/{id}/active") public Supplier active(@PathVariable String id,@RequestParam boolean value){Supplier s=repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Supplier not found"));s.setActive(value);return repo.save(s);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id){if(!repo.existsById(id))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Supplier not found");repo.deleteById(id);}
}
