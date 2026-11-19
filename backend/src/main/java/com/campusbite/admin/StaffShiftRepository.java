package com.campusbite.admin;
import org.springframework.data.mongodb.repository.MongoRepository;import java.util.List;import java.time.LocalDate;
public interface StaffShiftRepository extends MongoRepository<StaffShift,String>{List<StaffShift> findByStaffEmailOrderByShiftDateAsc(String email);List<StaffShift> findByOutletAndShiftDateOrderByStartTimeAsc(String outlet,LocalDate date);List<StaffShift> findAllByOrderByShiftDateAsc();}
