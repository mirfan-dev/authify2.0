package com.security.controller;

import com.security.dto.APIResponse;
import com.security.dto.ProfileRequest;
import com.security.dto.ProfileResponse;
import com.security.entity.User;
import com.security.service.UserService;
import com.security.service.impl.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping(value = "/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        emailService.sendEmail(request.getEmail(), request.getName());
        return userService.createUser(request);
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email){

        return userService.getProfile(email);

    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getUserById(@PathVariable String userId){

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<ProfileResponse> updateUser(
            @PathVariable String userId ,
            @RequestBody ProfileRequest request
            ){
        return ResponseEntity.ok(userService.updateUser(userId,request));
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<APIResponse> delete(@PathVariable String userId){

        userService.deleteUser(userId);
        APIResponse response=new APIResponse();
        return new ResponseEntity<>(response,HttpStatus.ACCEPTED);
    }

    @GetMapping("/pagination")
    public ResponseEntity<Page<ProfileResponse>> getEmployeeWithPagination(
            @RequestParam(value = "pageNumber", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "5") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<ProfileResponse>> searchEmployee(@PathVariable String keyword) {
        List<ProfileResponse> userDtos = userService.searchUsers(keyword);
        return ResponseEntity.ok(userDtos);
    }


}
