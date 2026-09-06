package com.example.employeemanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ─── Authentication Redirection ───────────────────────────────────────────

    @Test
    void unauthenticatedUserIsRedirectedFromDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void unauthenticatedUserIsRedirectedFromEmployees() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void unauthenticatedUserIsRedirectedFromDepartments() throws Exception {
        mockMvc.perform(get("/departments"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ─── Dashboard Access ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    // ─── Employee List Access ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessEmployeeList() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCanAccessEmployeeList() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());
    }

    // ─── Employee Management Authorization ────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessNewEmployeeForm() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotAccessNewEmployeeForm() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotDeleteEmployee() throws Exception {
        mockMvc.perform(post("/employees/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ─── Department Access ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessDepartmentList() throws Exception {
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCanAccessDepartmentList() throws Exception {
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAccessNewDepartmentForm() throws Exception {
        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotAccessNewDepartmentForm() throws Exception {
        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotDeleteDepartment() throws Exception {
        mockMvc.perform(post("/departments/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ─── Form Validation ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void invalidEmployeeFormIsRejected() throws Exception {
        mockMvc.perform(post("/employees")
                        .with(csrf())
                        .param("employeeCode", "")       // required but blank
                        .param("firstName", "")          // required but blank
                        .param("email", "not-an-email")) // invalid format
                .andExpect(status().isOk())              // returns to form
                .andExpect(model().attributeHasErrors("employeeForm"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void invalidDepartmentFormIsRejected() throws Exception {
        mockMvc.perform(post("/departments")
                        .with(csrf())
                        .param("name", ""))      // required but blank
                .andExpect(status().isOk())      // returns to form
                .andExpect(model().attributeHasErrors("departmentForm"));
    }

    // ─── Login Page ───────────────────────────────────────────────────────────

    @Test
    void loginPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}
