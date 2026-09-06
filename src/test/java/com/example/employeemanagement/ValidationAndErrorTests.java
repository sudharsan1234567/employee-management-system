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

/**
 * Phase I — Validation, error handling, and security edge-case tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidationAndErrorTests {

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

    // ─── 404 — Non-existent Resources ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void nonExistentEmployeeReturns404View() throws Exception {
        mockMvc.perform(get("/employees/999999"))
                .andExpect(status().isOk()) // controller returns 404 view with 200 (no @ResponseStatus)
                .andExpect(view().name("error/404"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void nonExistentDepartmentEditReturns404View() throws Exception {
        mockMvc.perform(get("/departments/999999/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void nonExistentEmployeeViewAsUserReturns404View() throws Exception {
        mockMvc.perform(get("/employees/999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    // ─── 403 — Role-based Access Denial ──────────────────────────────────────

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotAccessNewEmployeeFormGetsForbidden() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotPostNewEmployeeGetsForbidden() throws Exception {
        mockMvc.perform(post("/employees")
                        .with(csrf())
                        .param("employeeCode", "EMP-001")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotAccessNewDepartmentFormGetsForbidden() throws Exception {
        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotDeleteDepartmentGetsForbidden() throws Exception {
        mockMvc.perform(post("/departments/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"EMPLOYEE"})
    void userCannotDeleteEmployeeGetsForbidden() throws Exception {
        mockMvc.perform(post("/employees/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ─── Login / Logout ───────────────────────────────────────────────────────

    @Test
    void loginPageIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void loginWithInvalidCredentialsRedirectsToLoginError() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "wrong_user")
                        .param("password", "wrong_pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void logoutRedirectsToLoginWithLogoutParam() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ─── Employee Form Validation ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void blankEmployeeCodeIsRejected() throws Exception {
        mockMvc.perform(post("/employees")
                        .with(csrf())
                        .param("employeeCode", "")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeHasFieldErrors("employeeForm", "employeeCode"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void invalidEmailIsRejected() throws Exception {
        mockMvc.perform(post("/employees")
                        .with(csrf())
                        .param("employeeCode", "EMP-001")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeHasFieldErrors("employeeForm", "email"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void missingDepartmentIsRejected() throws Exception {
        mockMvc.perform(post("/employees")
                        .with(csrf())
                        .param("employeeCode", "EMP-001")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com")
                        .param("jobTitle", "Dev")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeHasFieldErrors("employeeForm", "departmentId"));
    }

    // ─── Department Form Validation ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void blankDepartmentNameIsRejected() throws Exception {
        mockMvc.perform(post("/departments")
                        .with(csrf())
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeHasFieldErrors("departmentForm", "name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void departmentNameTooLongIsRejected() throws Exception {
        String tooLong = "A".repeat(101);
        mockMvc.perform(post("/departments")
                        .with(csrf())
                        .param("name", tooLong))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeHasFieldErrors("departmentForm", "name"));
    }

    // ─── Error Pages ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void errorPage403IsAccessible() throws Exception {
        mockMvc.perform(get("/error/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void errorPage404IsAccessible() throws Exception {
        mockMvc.perform(get("/error/404"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    // ─── CSRF Protection ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void logoutWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }
}
