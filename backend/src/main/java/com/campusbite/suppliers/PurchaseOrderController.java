package com.campusbite.suppliers;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.List;
@RestController @RequestMapping("/api/purchases") @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
public class PurchaseOrderController {
 private final PurchaseOrderRepository purchases;private final SupplierRepository suppliers;
 public PurchaseOrderController(PurchaseOrderRepository p,SupplierRepository s){purchases=p;suppliers=s;}
 @GetMapping public List<PurchaseOrder> all(){return purchases.findAllByOrderByCreatedAtDesc();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public PurchaseOrder create(@Valid @RequestBody PurchaseOrderRequest r,Authentication a){Supplier s=suppliers.findById(r.supplierId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Supplier not found"));if(!s.isActive())throw new ResponseStatusException(HttpStatus.CONFLICT,"Supplier is inactive");var lines=r.items().stream().map(x->new PurchaseOrder.Line(x.itemName(),x.quantity(),x.unitCost())).toList();return purchases.save(new PurchaseOrder(s.getId(),s.getName(),lines,a.getName()));}
 @PatchMapping("/{id}/status") public PurchaseOrder status(@PathVariable String id,@RequestParam String value){if(!List.of("ORDERED","RECEIVED","CANCELLED").contains(value))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid purchase status");PurchaseOrder p=purchases.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Purchase order not found"));if(!"ORDERED".equals(p.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only ordered purchases can be updated");p.setStatus(value);return purchases.save(p);}
}
