package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.AttendanceStatus;
import com.example.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeAndAttendanceDate(Employee employee, LocalDate attendanceDate);

    List<Attendance> findByEmployeeOrderByAttendanceDateDesc(Employee employee);

    List<Attendance> findTop5ByEmployeeOrderByAttendanceDateDesc(Employee employee);

    List<Attendance> findAllByOrderByAttendanceDateDesc();

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:date IS NULL OR a.attendanceDate = :date) AND " +
           "(:employeeId IS NULL OR a.employee.id = :employeeId) AND " +
           "(:departmentId IS NULL OR a.employee.department.id = :departmentId) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.attendanceDate DESC, a.checkIn ASC")
    List<Attendance> filterAttendance(
            @Param("date") LocalDate date,
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("status") AttendanceStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Attendance a WHERE a.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
