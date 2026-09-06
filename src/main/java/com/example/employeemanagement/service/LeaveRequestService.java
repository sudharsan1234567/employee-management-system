package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.LeaveRequest;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestService {

    LeaveRequest applyLeave(Employee employee, String leaveType, LocalDate fromDate, LocalDate toDate, String reason);

    List<LeaveRequest> getEmployeeLeaves(Employee employee);

    List<LeaveRequest> getAllLeaves();

    LeaveRequest getLeaveById(Long id);

    void approveLeave(Long id, String adminResponse);

    void rejectLeave(Long id, String adminResponse);
}
