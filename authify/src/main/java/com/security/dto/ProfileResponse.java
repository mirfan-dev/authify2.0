package com.security.dto;

import com.security.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private String userId;

    private String name;

    private String email;

    private String gender;

    private Set<Role> roleEntities;

    private Boolean isAccountVerifiedAt;
}
