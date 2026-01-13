package com.example.users.services;

import com.example.users.dtos.UserDTO;
import com.example.users.dtos.UserDetailsDTO;
import com.example.users.entities.User;
import com.example.users.entities.roles.Role;
import com.example.users.handlers.exceptions.model.ResourceNotFoundException;
import com.example.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> findUsers() {
        return userRepository.findAll().stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));
        return UserBuilder.toUserDetailsDTO(user);
    }

    public UserDetailsDTO findUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(UserBuilder::toUserDetailsDTO).orElse(null);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    // Aceasta este metoda apelată de Controller pentru POST /people

    @Transactional
    public UUID insert(UserDetailsDTO userDetailsDTO, UUID id) {
        // 1. Prioritate ID-ului primit ca parametru (din Controller)
        // 2. Apoi ID-ului din DTO (dacă celălalt e null)
        UUID finalId = (id != null) ? id : userDetailsDTO.getId();

        if (finalId == null) {
            throw new RuntimeException("CRITICAL ERROR: user_id is NULL during profile creation!");
        }

        User user = new User();
        user.setId(finalId); // Aici se face legătura cu credentials-db
        user.setName(userDetailsDTO.getName());
        user.setEmail(userDetailsDTO.getEmail());
        user.setAge(userDetailsDTO.getAge());

        // Gestionare Rol
        if (userDetailsDTO.getRole() != null) {
            user.setRole(userDetailsDTO.getRole());
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);
        return user.getId();
    }

    @Transactional
    public void remove(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // Metode auxiliare (Update, etc.) pot rămâne neschimbate
    @Transactional
    public UserDetailsDTO updateFully(UUID id, UserDetailsDTO dto) {
        User entity = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setAge(dto.getAge());
        if (dto.getRole() != null) entity.setRole(dto.getRole());
        return UserBuilder.toUserDetailsDTO(userRepository.save(entity));
    }

    private static class UserBuilder {
        public static UserDTO toUserDTO(User user) {
            return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getRole());
        }
        public static UserDetailsDTO toUserDetailsDTO(User user) {
            return new UserDetailsDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getRole());
        }
    }
}