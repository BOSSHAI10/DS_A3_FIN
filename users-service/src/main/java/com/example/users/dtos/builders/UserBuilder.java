package com.example.users.dtos.builders;

import com.example.users.dtos.UserDTO;
import com.example.users.dtos.UserDetailsDTO;
import com.example.users.entities.User;

import java.util.UUID;

public class UserBuilder {

    private UserBuilder() {
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getRole());
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        return new UserDetailsDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getRole());
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        User user = new User();
        // Setăm ID-ul dacă există în DTO
        if (userDetailsDTO.getId() != null) {
            user.setId(userDetailsDTO.getId());
        }
        user.setName(userDetailsDTO.getName());
        user.setEmail(userDetailsDTO.getEmail());
        user.setAge(userDetailsDTO.getAge());
        user.setRole(userDetailsDTO.getRole());
        return user;
    }
}