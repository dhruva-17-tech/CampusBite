package com.campusbite.orders;
import org.springframework.data.mongodb.repository.MongoRepository; import java.time.Instant; import java.util.List;
public interface OrderRepository extends MongoRepository<CampusOrder,String>{
 List<CampusOrder> findByCustomerEmailOrderByCreatedAtDesc(String email);
 List<CampusOrder> findByOutletAndQueueDateAndStatusInOrderByQueueNumberAsc(String outlet,String queueDate,List<OrderStatus> statuses);
 List<CampusOrder> findByOutletAndQueueDateAndQueueNumberLessThanAndStatusInOrderByQueueNumberAsc(String outlet,String queueDate,long queueNumber,List<OrderStatus> statuses);
 List<CampusOrder> findAllByOrderByCreatedAtDesc(); long countByOutlet(String outlet);
 long countByStatus(OrderStatus status); long countByOutletAndStatusIn(String outlet,List<OrderStatus> statuses);
 List<CampusOrder> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant start,Instant end);
}
