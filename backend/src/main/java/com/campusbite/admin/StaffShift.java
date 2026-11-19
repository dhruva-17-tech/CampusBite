package com.campusbite.admin;
import org.springframework.data.annotation.Id;import org.springframework.data.mongodb.core.mapping.Document;import java.time.Instant;import java.time.LocalDate;
@Document("staff_shifts")
public class StaffShift {
 @Id private String id;private String staffEmail;private String staffName;private String outlet;private LocalDate shiftDate;private String startTime;private String endTime;private String notes;private Instant createdAt=Instant.now();
 public StaffShift(){}public StaffShift(String email,String name,String outlet,LocalDate date,String start,String end,String notes){staffEmail=email;staffName=name;this.outlet=outlet;shiftDate=date;startTime=start;endTime=end;this.notes=notes;}
 public String getId(){return id;}public String getStaffEmail(){return staffEmail;}public String getStaffName(){return staffName;}public String getOutlet(){return outlet;}public LocalDate getShiftDate(){return shiftDate;}public String getStartTime(){return startTime;}public String getEndTime(){return endTime;}public String getNotes(){return notes;}public Instant getCreatedAt(){return createdAt;}
}
