package com.campusbite.admin;
import jakarta.validation.constraints.*;import java.time.LocalDate;
public record StaffShiftRequest(@Email @NotBlank String staffEmail,@NotNull LocalDate shiftDate,@NotBlank String startTime,@NotBlank String endTime,String notes){}
