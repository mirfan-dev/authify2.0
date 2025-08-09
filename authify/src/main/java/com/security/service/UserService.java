package com.security.service;

import com.security.dto.ProfileRequest;
import com.security.dto.ProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {


    public ProfileResponse createUser(ProfileRequest profileRequest);

    public ProfileResponse getProfile(String email);

    public ProfileResponse getUserById(String userId);

    public ProfileResponse updateUser(String userId, ProfileRequest profileRequest);


    void deleteUser(String userId);

    // --- Pagination & Sorting ---
    Page<ProfileResponse> getAllUsers(Pageable pageable);

    // --- Search ---
    List<ProfileResponse> searchUsers(String keyword);
}
