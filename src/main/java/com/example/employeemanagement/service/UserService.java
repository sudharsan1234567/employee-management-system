package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.User;
import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    User createUser(User user);
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
}
