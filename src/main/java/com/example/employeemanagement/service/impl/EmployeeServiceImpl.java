package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmployeeStatus;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.AttendanceRepository;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.LeaveRequestRepository;
import com.example.employeemanagement.service.EmployeeService;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AttendanceRepository attendanceRepository,
            LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    @Override
    @Transactional
    public Employee createEmployee(Employee employee) {
        checkUniqueConstraints(employee.getEmployeeCode(), employee.getEmail(), null);
        
        Department department = departmentRepository.findById(employee.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employee.getDepartment().getId()));
        
        if (employee.getStatus() == null) {
            throw new BusinessRuleException("Employee status is required.");
        }
        
        employee.setDepartment(department);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee createEmployeeWithLogin(Employee employee, String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessRuleException("Username cannot be empty.");
        }
        if (rawPassword == null || rawPassword.trim().length() < 6) {
            throw new BusinessRuleException("Password must be at least 6 characters long.");
        }
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new DuplicateResourceException("Username '" + username.trim() + "' is already taken.");
        }

        Employee savedEmployee = createEmployee(employee);

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.EMPLOYEE);
        user.setEnabled(true);
        user.setEmployee(savedEmployee);
        userRepository.save(user);

        savedEmployee.setUser(user);
        return savedEmployee;
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee existingEmployee = getEmployeeById(id);
        
        checkUniqueConstraints(employeeDetails.getEmployeeCode(), employeeDetails.getEmail(), id);
        
        Department department = departmentRepository.findById(employeeDetails.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employeeDetails.getDepartment().getId()));
        
        if (employeeDetails.getStatus() == null) {
            throw new BusinessRuleException("Employee status is required.");
        }
        
        existingEmployee.setEmployeeCode(employeeDetails.getEmployeeCode());
        existingEmployee.setFirstName(employeeDetails.getFirstName());
        existingEmployee.setLastName(employeeDetails.getLastName());
        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setPhone(employeeDetails.getPhone());
        existingEmployee.setDateOfBirth(employeeDetails.getDateOfBirth());
        existingEmployee.setGender(employeeDetails.getGender());
        existingEmployee.setAddress(employeeDetails.getAddress());
        existingEmployee.setHireDate(employeeDetails.getHireDate());
        existingEmployee.setJobTitle(employeeDetails.getJobTitle());
        existingEmployee.setSalary(employeeDetails.getSalary());
        existingEmployee.setStatus(employeeDetails.getStatus());
        existingEmployee.setDepartment(department);
        
        return employeeRepository.save(existingEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        // Verify employee exists
        getEmployeeById(id);
        // Delete in dependency order to avoid FK constraint violations:
        // 1. Leave requests (reference employee)
        leaveRequestRepository.deleteByEmployeeId(id);
        // 2. Attendance records (reference employee)
        attendanceRepository.deleteByEmployeeId(id);
        // 3. User/login account (reference employee)
        userRepository.deleteByEmployeeId(id);
        // 4. Finally delete the employee
        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee findByEmployeeCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employeeCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String firstName, String lastName) {
        return employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(firstName, lastName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> filterByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> filterByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return employeeRepository.count();
    }

    private void checkUniqueConstraints(String employeeCode, String email, Long currentEmployeeId) {
        Optional<Employee> byCode = employeeRepository.findByEmployeeCode(employeeCode);
        if (byCode.isPresent() && !byCode.get().getId().equals(currentEmployeeId)) {
            throw new DuplicateResourceException("Employee with code '" + employeeCode + "' already exists.");
        }
        
        Optional<Employee> byEmail = employeeRepository.findByEmail(email);
        if (byEmail.isPresent() && !byEmail.get().getId().equals(currentEmployeeId)) {
            throw new DuplicateResourceException("Employee with email '" + email + "' already exists.");
        }
    }
}
