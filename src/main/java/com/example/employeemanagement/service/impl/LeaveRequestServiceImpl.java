package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.LeaveRequest;
import com.example.employeemanagement.entity.LeaveStatus;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.LeaveRequestRepository;
import com.example.employeemanagement.service.LeaveRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Override
    @Transactional
    public LeaveRequest applyLeave(Employee employee, String leaveType, LocalDate fromDate, LocalDate toDate, String reason) {
        if (leaveType == null || leaveType.isBlank()) {
            throw new BusinessRuleException("Leave type is required.");
        }
        if (fromDate == null || toDate == null) {
            throw new BusinessRuleException("From date and To date are required.");
        }
        if (toDate.isBefore(fromDate)) {
            throw new BusinessRuleException("To date cannot be before From date.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("Reason is required.");
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(leaveType);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setReason(reason);
        request.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeeLeaves(Employee employee) {
        return leaveRequestRepository.findByEmployeeOrderByFromDateDesc(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByAppliedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequest getLeaveById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
    }

    @Override
    @Transactional
    public void approveLeave(Long id, String adminResponse) {
        LeaveRequest request = getLeaveById(id);
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING leave requests can be approved.");
        }
        request.setStatus(LeaveStatus.APPROVED);
        request.setAdminResponse(adminResponse);
        request.setRespondedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectLeave(Long id, String adminResponse) {
        LeaveRequest request = getLeaveById(id);
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING leave requests can be rejected.");
        }
        request.setStatus(LeaveStatus.REJECTED);
        request.setAdminResponse(adminResponse);
        request.setRespondedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
    }
}
