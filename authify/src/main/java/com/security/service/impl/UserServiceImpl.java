package com.security.service.impl;

import com.security.dto.ProfileRequest;
import com.security.dto.ProfileResponse;
import com.security.entity.Role;
import com.security.entity.User;
import com.security.exception.UserException;
import com.security.repository.RoleRepository;
import com.security.repository.UserRepository;
import com.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public ProfileResponse createUser(ProfileRequest profileRequest) {

        // 1️⃣ Email check
        if (userRepository.existsByEmail(profileRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // 2️⃣ Prepare roles
        Set<Role> roles = new HashSet<>();
//        if (profileRequest.getRoleEntities() == null || profileRequest.getRoleEntities().isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one role must be assigned");
//        }


        // 3️⃣ Map DTO to entity
        User newUser = modelMapper.map(profileRequest, User.class);
        newUser.setUserId(UUID.randomUUID().toString());
        newUser.setPassword(passwordEncoder.encode(profileRequest.getPassword()));
        newUser.setRoleEntities(profileRequest.getRoleEntities());

        // 4️⃣ Save & return
        User savedUser = userRepository.save(newUser);
        return modelMapper.map(savedUser, ProfileResponse.class);
    }




    @Override
    public ProfileResponse getProfile(String email) {

        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found with this email "+email));
        return modelMapper.map(user,ProfileResponse.class);
    }

    @Override
    public ProfileResponse getUserById(String userId) {
        User user= userRepository.findByUserId(userId)
                .orElseThrow(()-> new UserException("User Not found with this id "+userId));
        return modelMapper.map(user,ProfileResponse.class);
    }

    @Override
    public ProfileResponse updateUser(String userId, ProfileRequest profileRequest) {

        // Fetch user entity
        User existingUser= userRepository.findByUserId(userId)
                .orElseThrow(()-> new UserException("User Not found with this id "+userId));

        // Update fields
        existingUser.setEmail(profileRequest.getEmail());
        existingUser.setGender(profileRequest.getGender());
        existingUser.setName(profileRequest.getName());
        existingUser.setImages(profileRequest.getImages());

        // Save updated user
        User updatedUser = userRepository.save(existingUser);

        // Convert to response DTO
        return modelMapper.map(updatedUser, ProfileResponse.class);
    }

    @Override
    public void deleteUser(String userId) {
        User existingUser= userRepository.findByUserId(userId)
                .orElseThrow(()-> new UserException("User Not found with this id "+userId));
        userRepository.delete(existingUser);


    }

    @Override
    public Page<ProfileResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage=userRepository.findAll(pageable);

        return userPage.map((user) ->modelMapper.map(user,ProfileResponse.class));
    }

    @Override
    public List<ProfileResponse> searchUsers(String keyword) {
        List<User> users=  userRepository.searchUser(keyword);

        return users.stream().map((user) -> modelMapper.map(user,ProfileResponse.class))
                .toList();
    }

}
