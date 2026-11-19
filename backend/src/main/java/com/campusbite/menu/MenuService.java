package com.campusbite.menu;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class MenuService {
    private static final Set<String> OUTLETS = Set.of(
            "Normal Campus Canteen", "Normal Canteen", "Nescafé", "Amul", "Juice Corner");
    private final MenuItemRepository repository;

    public MenuService(MenuItemRepository repository) {
        this.repository = repository;
    }

    public List<MenuItem> list(String outlet) {
        if (outlet == null || outlet.isBlank())
            return repository.findAllByOrderByOutletAscNameAsc();
        validateOutlet(outlet);
        return repository.findByOutletIgnoreCaseOrderByNameAsc(outlet.trim());
    }

    public MenuItem get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));
    }

    public MenuItem create(MenuItemRequest request) {
        validateOutlet(request.outlet());
        return repository.save(new MenuItem(request.name().trim(), safe(request.description()),
                request.category().trim(), request.price(), request.stock(), request.outlet().trim(),
                safe(request.imageUrl()), request.available()));
    }

    public MenuItem update(String id, MenuItemRequest request) {
        validateOutlet(request.outlet());
        MenuItem item = get(id);
        item.update(request.name().trim(), safe(request.description()), request.category().trim(),
                request.price(), request.outlet().trim(), safe(request.imageUrl()), request.available());
        item.setStock(request.stock());
        return repository.save(item);
    }

    public void delete(String id) {
        repository.delete(get(id)); // 404 if it does not exist
    }

    public MenuItem setStock(String id, int quantity) {
        if (quantity < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock cannot be negative");
        MenuItem item = get(id);
        item.setStock(quantity);
        if (quantity == 0)
            item.setAvailable(false);
        return repository.save(item);
    }

    public MenuItem setAvailability(String id, boolean available) {
        MenuItem item = get(id);
        item.setAvailable(available && item.getStock() > 0);
        return repository.save(item);
    }

    public void deductStock(MenuItem item, int quantity) {
        if (quantity <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        if (!item.isAvailable() || item.getStock() < quantity)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    item.getName() + " is unavailable or has insufficient stock");
        item.setStock(item.getStock() - quantity);
        if (item.getStock() == 0)
            item.setAvailable(false);
        repository.save(item);
    }


    public List<MenuItem> lowStock(int threshold) { return repository.findByStockLessThanEqualOrderByStockAsc(threshold); }
    public MenuItem addStock(String id, int quantity) {
        if (quantity <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        MenuItem item = get(id); item.setStock(item.getStock() + quantity);
        if (item.getStock() > 0) item.setAvailable(true);
        return repository.save(item);
    }

    private void validateOutlet(String outlet) {
        if (outlet == null || !OUTLETS.contains(outlet.trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Outlet must be one of: " + String.join(", ", OUTLETS));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
