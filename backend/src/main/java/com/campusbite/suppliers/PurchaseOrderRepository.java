package com.campusbite.suppliers;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface PurchaseOrderRepository extends MongoRepository<PurchaseOrder,String>{List<PurchaseOrder> findAllByOrderByCreatedAtDesc();}
