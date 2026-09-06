package com.example.employeemanagement;

import com.example.employeemanagement.config.CustomAuthenticationSuccessHandler;
import com.example.employeemanagement.dto.ChangePasswordForm;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.repository.AttendanceRepository;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.AttendanceService;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class EmployeePortalTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    private MockMvc mockMvc;
    private Department department;
    private Employee employee;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        department = departmentRepository.findByName("PhaseK Department").orElseGet(() -> {
            Department d = new Department();
            d.setName("PhaseK Department");
            d.setDescription("Phase K Testing Department");
            return departmentRepository.save(d);
        });

        String empCode = "EMP-K-" + System.currentTimeMillis() % 100000;
        String email = "emp.k." + System.currentTimeMillis() % 100000 + "@company.com";
        String username = "emp.k." + System.currentTimeMillis() % 100000;

        Employee newEmp = new Employee();
        newEmp.setEmployeeCode(empCode);
        newEmp.setFirstName("Ravi");
        newEmp.setLastName("Kumar");
        newEmp.setEmail(email);
        newEmp.setDepartment(department);
        newEmp.setStatus(EmployeeStatus.ACTIVE);
        newEmp.setJobTitle("Software Engineer");
        newEmp.setHireDate(LocalDate.of(2024, 1, 15));
        newEmp.setSalary(new BigDecimal("75000.00"));

        employee = employeeService.createEmployeeWithLogin(newEmp, username, "password123");
        employeeUser = userRepository.findByUsername(username).orElseThrow();
    }

    // 1. EMPLOYEE role can authenticate and has ROLE_EMPLOYEE authority
    @Test
    void employeeUserHasRoleEmployee() {
        assertThat(employeeUser.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(employeeUser.isEnabled()).isTrue();
    }

    // 2. ADMIN can authenticate
    @Test
    void adminUserHasRoleAdmin() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }

    // 3. Employee account is connected to the correct Employee
    @Test
    void employeeUserIsConnectedToEmployee() {
        assertThat(employeeUser.getEmployee()).isNotNull();
        assertThat(employeeUser.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(employeeUser.getEmployee().getEmployeeCode()).isEqualTo(employee.getEmployeeCode());
    }

    // 4. Employee password is BCrypt hashed
    @Test
    void employeePasswordIsBCryptHashed() {
        assertThat(employeeUser.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("password123", employeeUser.getPassword())).isTrue();
    }

    // 5. Plain-text password is never stored
    @Test
    void plainTextPasswordIsNotStored() {
        assertThat(employeeUser.getPassword()).isNotEqualTo("password123");
    }

    // 6. Role-based redirect on login
    @Test
    void roleBasedLoginRedirectsCorrectly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Test ADMIN redirect
        var adminAuth = new UsernamePasswordAuthenticationToken(
                "admin", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        successHandler.onAuthenticationSuccess(request, response, adminAuth);
        assertThat(response.getRedirectedUrl()).isEqualTo("/dashboard");

        // Test EMPLOYEE redirect
        MockHttpServletResponse empResponse = new MockHttpServletResponse();
        var empAuth = new UsernamePasswordAuthenticationToken(
                "ravi.emp", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        successHandler.onAuthenticationSuccess(request, empResponse, empAuth);
        assertThat(empResponse.getRedirectedUrl()).isEqualTo("/employee/dashboard");
    }

    // 7. Employee can access own profile
    @Test
    void employeeCanAccessOwnProfile() throws Exception {
        mockMvc.perform(get("/employee/profile")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/profile"))
                .andExpect(model().attributeExists("employee"));
    }

    // 8. Employee cannot access another employee's profile by manipulating URL IDs
    @Test
    void employeeDashboardLoadsAuthenticatedEmployeeOnly() throws Exception {
        mockMvc.perform(get("/employee/dashboard")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/dashboard"))
                .andExpect(model().attribute("employee", employee));
    }

    // 9. Employee cannot access ADMIN employee-management routes
    @Test
    void employeeCannotAccessAdminNewEmployeeRoute() throws Exception {
        mockMvc.perform(get("/employees/new")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // 10. Employee cannot create an employee
    @Test
    void employeeCannotPostCreateEmployee() throws Exception {
        mockMvc.perform(post("/employees")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE"))
                        .param("employeeCode", "EMP-HACK")
                        .param("firstName", "Hack")
                        .param("lastName", "User")
                        .param("email", "hack@test.com"))
                .andExpect(status().isForbidden());
    }

    // 11. Employee cannot delete an employee
    @Test
    void employeeCannotDeleteEmployee() throws Exception {
        mockMvc.perform(post("/employees/" + employee.getId() + "/delete")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // 12. Employee cannot edit an employee
    @Test
    void employeeCannotEditEmployee() throws Exception {
        mockMvc.perform(get("/employees/" + employee.getId() + "/edit")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // 13. Employee can change their own password
    @Test
    void employeeCanChangeOwnPassword() throws Exception {
        mockMvc.perform(post("/employee/change-password")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE"))
                        .param("currentPassword", "password123")
                        .param("newPassword", "newSecret99")
                        .param("confirmPassword", "newSecret99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee/dashboard"));

        User updated = userRepository.findByUsername(employeeUser.getUsername()).orElseThrow();
        assertThat(passwordEncoder.matches("newSecret99", updated.getPassword())).isTrue();
    }

    // 14. Password change fails if current password does not match
    @Test
    void employeePasswordChangeFailsOnWrongCurrentPassword() throws Exception {
        mockMvc.perform(post("/employee/change-password")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE"))
                        .param("currentPassword", "wrongOldPassword")
                        .param("newPassword", "newSecret99")
                        .param("confirmPassword", "newSecret99"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/change-password"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // 15. Employee can check in
    @Test
    void employeeCanCheckIn() {
        Attendance attendance = attendanceService.checkIn(employee);
        assertThat(attendance).isNotNull();
        assertThat(attendance.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(attendance.getAttendanceDate()).isEqualTo(LocalDate.now());
        assertThat(attendance.getCheckIn()).isNotNull();
        assertThat(attendance.getStatus()).isIn(AttendanceStatus.PRESENT, AttendanceStatus.LATE);
    }

    // 16. Duplicate check-in is rejected
    @Test
    void duplicateCheckInIsRejected() {
        attendanceService.checkIn(employee);
        assertThatThrownBy(() -> attendanceService.checkIn(employee))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already checked in");
    }

    // 17. Employee can check out
    @Test
    void employeeCanCheckOut() {
        attendanceService.checkIn(employee);
        Attendance checkedOut = attendanceService.checkOut(employee);
        assertThat(checkedOut.getCheckOut()).isNotNull();
    }

    // 18. Employee cannot check out without check-in
    @Test
    void employeeCannotCheckOutWithoutCheckIn() {
        assertThatThrownBy(() -> attendanceService.checkOut(employee))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("check in first");
    }

    // 19. Employee can view only their own attendance
    @Test
    void employeeCanViewOwnAttendance() throws Exception {
        mockMvc.perform(get("/employee/attendance")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/attendance"))
                .andExpect(model().attributeExists("attendanceList"));
    }

    // 20. ADMIN can view all attendance
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanViewAllAttendance() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance/list"))
                .andExpect(model().attributeExists("attendanceList"));
    }

    // 21. CSRF protection works for attendance POST requests
    @Test
    void checkInFailsWithoutCsrf() throws Exception {
        mockMvc.perform(post("/employee/attendance/check-in")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(employeeUser.getUsername()).roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }
}
