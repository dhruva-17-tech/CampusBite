package com.campusbite.suppliers;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface SupplierRepository extends MongoRepository<Supplier,String> { List<Supplier> findByActiveTrueOrderByNameAsc(); }
