package com.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private String userId;

    private String name;

    private String email;

    private String gender;

    private Boolean isAccountVerifiedAt;
}
