package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.AttendanceStatus;
import com.example.employeemanagement.entity.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {

    Attendance checkIn(Employee employee);

    Attendance checkOut(Employee employee);

    Optional<Attendance> getTodayAttendance(Employee employee);

    List<Attendance> getEmployeeAttendanceHistory(Employee employee);

    List<Attendance> getEmployeeRecentAttendance(Employee employee, int limit);

    List<Attendance> getAllAttendance(LocalDate date, Long employeeId, Long departmentId, AttendanceStatus status);

    Attendance getAttendanceById(Long id);

    Attendance updateAttendance(Long id, LocalDateTime checkIn, LocalDateTime checkOut);

    void deleteAttendance(Long id);
}
