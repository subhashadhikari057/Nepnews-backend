package com.nepnews.services;

import com.nepnews.models.User;
import com.nepnews.models.enums.Role;
import com.nepnews.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(String id, String role) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(Role.valueOf(role.toUpperCase())); // Must match enum (READER, AUTHOR, EDITOR, ADMIN)
        return userRepository.save(user);
    }
    public boolean deleteUser(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
