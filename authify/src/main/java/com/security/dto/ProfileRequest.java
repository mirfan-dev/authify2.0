package com.security.dto;

import com.security.util.ValidGender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {


    @NotBlank(message = "Name should not be empty")
    private String name;

    @Email(message = "Enter valid email address")
    @NotNull(message = "Email should not be empty")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Gender should not be empty")
    @ValidGender(message = "'Male', 'Female' and 'Other' are only allowed")
    private String gender;

    @NotEmpty(message = "At least one image is required")
    private List<@NotBlank(message = "Image URL must not be blank") String> images;
}
