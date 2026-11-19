package com.campusbite.admin;
import com.campusbite.users.*;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;import java.util.*;import java.time.*;
@RestController @RequestMapping("/api/shifts")
public class StaffShiftController {
 private final StaffShiftRepository shifts;private final UserRepository users;
 public StaffShiftController(StaffShiftRepository s,UserRepository u){shifts=s;users=u;}
 @GetMapping("/my") @PreAuthorize("hasRole('STAFF')") public List<StaffShift> mine(Authentication a){return shifts.findByStaffEmailOrderByShiftDateAsc(a.getName());}
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','OWNER')") public List<StaffShift> all(){return shifts.findAllByOrderByShiftDateAsc();}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','OWNER')") @ResponseStatus(HttpStatus.CREATED) public StaffShift create(@Valid @RequestBody StaffShiftRequest r){AppUser staff=users.findByEmail(r.staffEmail().trim().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Staff user not found"));if(staff.getRole()!=Role.STAFF)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Shifts can be assigned only to staff");if(!r.endTime().matches("([01]\\d|2[0-3]):[0-5]\\d")||!r.startTime().matches("([01]\\d|2[0-3]):[0-5]\\d")||r.startTime().compareTo(r.endTime())>=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use valid 24-hour times; end must be after start");return shifts.save(new StaffShift(staff.getEmail(),staff.getName(),staff.getAssignedOutlet(),r.shiftDate(),r.startTime(),r.endTime(),r.notes()));}
 @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','OWNER')") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id){if(!shifts.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Shift not found");shifts.deleteById(id);}
}
