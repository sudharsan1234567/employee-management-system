package com.example.employeemanagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler — safety net for any exception that leaks out of controllers.
 * Controllers already handle their own specific exceptions (ResourceNotFoundException,
 * DuplicateResourceException, BusinessRuleException) locally to preserve BindingResult
 * and form state. This handler catches anything that escapes.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Safety net for ResourceNotFoundException that leaks past controller-level handling.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Safety net for DuplicateResourceException that leaks past controller-level handling.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDuplicateResource(DuplicateResourceException ex, Model model) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    /**
     * Safety net for BusinessRuleException that leaks past controller-level handling.
     */
    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessRule(BusinessRuleException ex, Model model) {
        log.warn("Business rule violation: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    /**
     * Handle authorization exceptions triggered by @PreAuthorize on controller methods.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        log.warn("Access denied: {}", ex.getMessage());
        return "error/403";
    }

    /**
     * Catch-all for any unexpected exception — logs the full stack trace server-side
     * but shows only a generic message to the user.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model) {
        log.error("Unexpected application error", ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        return "error";
    }
}
