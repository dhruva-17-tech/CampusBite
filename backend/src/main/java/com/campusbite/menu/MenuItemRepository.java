package com.campusbite.menu;
import org.springframework.data.mongodb.repository.MongoRepository; import java.util.List;
public interface MenuItemRepository extends MongoRepository<MenuItem,String>{
 List<MenuItem> findAllByOrderByOutletAscNameAsc();
 List<MenuItem> findByOutletIgnoreCaseOrderByNameAsc(String outlet);
 List<MenuItem> findByStockLessThanEqualOrderByStockAsc(int threshold);
 long countByOutlet(String outlet);
}
