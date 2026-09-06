package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.AttendanceStatus;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.AttendanceRepository;
import com.example.employeemanagement.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime WORK_START_GRACE_PERIOD = LocalTime.of(9, 30);

    private final AttendanceRepository attendanceRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional
    public Attendance checkIn(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new BusinessRuleException("Valid employee is required for check-in.");
        }

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeAndAttendanceDate(employee, today);
        if (existing.isPresent()) {
            throw new BusinessRuleException("You have already checked in for today (" + today + ").");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status = now.toLocalTime().isAfter(WORK_START_GRACE_PERIOD)
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT;

        Attendance attendance = new Attendance(employee, today, now, status);
        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public Attendance checkOut(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new BusinessRuleException("Valid employee is required for check-out.");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeAndAttendanceDate(employee, today)
                .orElseThrow(() -> new BusinessRuleException("No check-in record found for today. Please check in first."));

        if (attendance.getCheckOut() != null) {
            throw new BusinessRuleException("You have already checked out for today.");
        }

        attendance.setCheckOut(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Attendance> getTodayAttendance(Employee employee) {
        if (employee == null) {
            return Optional.empty();
        }
        return attendanceRepository.findByEmployeeAndAttendanceDate(employee, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getEmployeeAttendanceHistory(Employee employee) {
        return attendanceRepository.findByEmployeeOrderByAttendanceDateDesc(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getEmployeeRecentAttendance(Employee employee, int limit) {
        List<Attendance> list = attendanceRepository.findByEmployeeOrderByAttendanceDateDesc(employee);
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAllAttendance(LocalDate date, Long employeeId, Long departmentId, AttendanceStatus status) {
        return attendanceRepository.filterAttendance(date, employeeId, departmentId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
    }

    @Override
    @Transactional
    public Attendance updateAttendance(Long id, LocalDateTime checkIn, LocalDateTime checkOut) {
        Attendance attendance = getAttendanceById(id);
        if (checkIn == null) {
            throw new BusinessRuleException("Check-in time is required.");
        }
        if (checkOut != null && checkOut.isBefore(checkIn)) {
            throw new BusinessRuleException("Check-out time cannot be before check-in time.");
        }
        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);
        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        Attendance attendance = getAttendanceById(id);
        attendanceRepository.delete(attendance);
    }
}
