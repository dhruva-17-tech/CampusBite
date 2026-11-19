package com.campusbite.requests;
import org.springframework.data.mongodb.repository.MongoRepository;import java.util.List;
public interface CookingRequestRepository extends MongoRepository<CookingRequest,String>{List<CookingRequest> findByCustomerEmailOrderByCreatedAtDesc(String email);List<CookingRequest> findByOutletAndStatusNotOrderByCreatedAtAsc(String outlet,String status);List<CookingRequest> findAllByOrderByCreatedAtDesc();}
